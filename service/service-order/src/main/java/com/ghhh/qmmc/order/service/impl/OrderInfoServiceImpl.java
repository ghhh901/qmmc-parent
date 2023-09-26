package com.ghhh.qmmc.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghhh.qmmc.activity.client.ActivityFeignClient;
import com.ghhh.qmmc.cart.client.CartFeignClient;
import com.ghhh.qmmc.client.product.ProductFeignClient;
import com.ghhh.qmmc.client.user.UserFeignClient;
import com.ghhh.qmmc.common.DateUtil;
import com.ghhh.qmmc.common.constant.MqConst;
import com.ghhh.qmmc.common.constant.RedisConst;
import com.ghhh.qmmc.common.security.AuthContextHolder;
import com.ghhh.qmmc.common.service.RabbitService;
import com.ghhh.qmmc.enums.*;
import com.ghhh.qmmc.exception.QmmcException;
import com.ghhh.qmmc.model.activity.ActivityRule;
import com.ghhh.qmmc.model.activity.CouponInfo;
import com.ghhh.qmmc.model.order.CartInfo;
import com.ghhh.qmmc.model.order.OrderInfo;
import com.ghhh.qmmc.model.order.OrderItem;
import com.ghhh.qmmc.order.service.OrderInfoService;
import com.ghhh.qmmc.order.mapper.OrderInfoMapper;
import com.ghhh.qmmc.order.service.OrderItemService;
import com.ghhh.qmmc.result.ResultCodeEnum;
import com.ghhh.qmmc.vo.order.CartInfoVo;
import com.ghhh.qmmc.vo.order.OrderConfirmVo;
import com.ghhh.qmmc.vo.order.OrderSubmitVo;
import com.ghhh.qmmc.vo.product.SkuStockLockVo;
import com.ghhh.qmmc.vo.user.LeaderAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo>
    implements OrderInfoService{

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ActivityFeignClient activityFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitService rabbitService;

    //确认订单
    @Override
    public OrderConfirmVo confirmOrder() {
        Long userId = AuthContextHolder.getUserId();
        //获取团长信息
        LeaderAddressVo leaderAddressVo = userFeignClient.getLeaderAddressVoByUserId(userId);
        //通过购物车获取用户要购买的商品
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        // 防重：生成一个唯一标识，保存到redis中一份
        String orderNo = System.currentTimeMillis() + "";
        redisTemplate.opsForValue().set(RedisConst.ORDER_REPEAT + orderNo,orderNo,24, TimeUnit.HOURS);
        //获取购物车满足条件的促销与优惠券信息
        OrderConfirmVo orderConfirmVo = activityFeignClient.findCartActivityAndCoupon(cartCheckedList, userId);
        orderConfirmVo.setLeaderAddressVo(leaderAddressVo);
        orderConfirmVo.setOrderNo(orderNo);
        return orderConfirmVo;
    }

    //生成订单
    @Override
    public Long submitOrder(OrderSubmitVo orderParamVo) {
        //1.添加当前用户
        orderParamVo.setUserId(AuthContextHolder.getUserId());

        //2.防止重复提交：redis
        String orderNo = orderParamVo.getOrderNo();
        if (StringUtils.isEmpty(orderNo)){
            throw new QmmcException(ResultCodeEnum.ILLEGAL_REQUEST);
        }

        String script = "if(redis.call('get', KEYS[1]) == ARGV[1]) then return redis.call('del', KEYS[1]) else return 0 end";
        Boolean flag =(Boolean) redisTemplate.execute(new DefaultRedisScript(script, Boolean.class),
                                                            Arrays.asList(RedisConst.ORDER_REPEAT + orderNo), orderNo);
        if (!flag){
            throw new QmmcException(ResultCodeEnum.REPEAT_SUBMIT);
        }

        //3 普通商品
        List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(AuthContextHolder.getUserId());
        List<CartInfo> commonSkuList = cartInfoList.stream().filter(cartInfo -> cartInfo.getSkuType() == SkuType.COMMON.getCode()).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(commonSkuList)) {
            List<SkuStockLockVo> commonStockLockVoList = commonSkuList.stream().map(item -> {
                SkuStockLockVo skuStockLockVo = new SkuStockLockVo();
                skuStockLockVo.setSkuId(item.getSkuId());
                skuStockLockVo.setSkuNum(item.getSkuNum());
                return skuStockLockVo;
            }).collect(Collectors.toList());
            //是否锁定
            Boolean isLockCommon = productFeignClient.checkAndLock(commonStockLockVoList, orderParamVo.getOrderNo());
            if (!isLockCommon){
                throw new QmmcException(ResultCodeEnum.ORDER_STOCK_FALL);
            }
        }
        //4.下单
        Long saveId = this.saveOrder(orderParamVo,cartInfoList);

        rabbitService.sendMessage(MqConst.EXCHANGE_ORDER_DIRECT, MqConst.ROUTING_DELETE_CART, orderParamVo.getUserId());


        return saveId;
    }

    //向两张表中添加数据
    @Transactional(rollbackFor = {Exception.class})
    public Long saveOrder(OrderSubmitVo orderSubmitVo, List<CartInfo> cartInfoList) {
        Long userId = AuthContextHolder.getUserId();
        if(CollectionUtils.isEmpty(cartInfoList)) {
            throw new QmmcException(ResultCodeEnum.DATA_ERROR);
        }
        LeaderAddressVo leaderAddressVo = userFeignClient.getLeaderAddressVoByUserId(userId);
        if(null == leaderAddressVo) {
            throw new QmmcException(ResultCodeEnum.DATA_ERROR);
        }

        //计算购物项分摊的优惠减少金额，按比例分摊，退款时按实际支付金额退款
        Map<String, BigDecimal> activitySplitAmountMap = this.computeActivitySplitAmount(cartInfoList);
        Map<String, BigDecimal> couponInfoSplitAmountMap = this.computeCouponInfoSplitAmount(cartInfoList, orderSubmitVo.getCouponId());
        //sku对应的订单明细
        List<OrderItem> orderItemList = new ArrayList<>();
        // 保存订单明细
        for (CartInfo cartInfo : cartInfoList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setId(null);
            orderItem.setCategoryId(cartInfo.getCategoryId());
            if(cartInfo.getSkuType() == SkuType.COMMON.getCode()) {
                orderItem.setSkuType(SkuType.COMMON);
            } else {
                orderItem.setSkuType(SkuType.SECKILL);
            }
            orderItem.setSkuId(cartInfo.getSkuId());
            orderItem.setSkuName(cartInfo.getSkuName());
            orderItem.setSkuPrice(cartInfo.getCartPrice());
            orderItem.setImgUrl(cartInfo.getImgUrl());
            orderItem.setSkuNum(cartInfo.getSkuNum());
            orderItem.setLeaderId(orderSubmitVo.getLeaderId());

            //促销活动分摊金额
            BigDecimal splitActivityAmount = activitySplitAmountMap.get("activity:"+orderItem.getSkuId());
            if(null == splitActivityAmount) {
                splitActivityAmount = new BigDecimal(0);
            }
            orderItem.setSplitActivityAmount(splitActivityAmount);

            //优惠券分摊金额
            BigDecimal splitCouponAmount = couponInfoSplitAmountMap.get("coupon:"+orderItem.getSkuId());
            if(null == splitCouponAmount) {
                splitCouponAmount = new BigDecimal(0);
            }
            orderItem.setSplitCouponAmount(splitCouponAmount);

            //优惠后的总金额
            BigDecimal skuTotalAmount = orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuNum()));
            BigDecimal splitTotalAmount = skuTotalAmount.subtract(splitActivityAmount).subtract(splitCouponAmount);
            orderItem.setSplitTotalAmount(splitTotalAmount);
            orderItemList.add(orderItem);
        }

        //保存订单
        OrderInfo order = new OrderInfo();
        order.setUserId(userId);
//		private String nickName;
        order.setOrderNo(orderSubmitVo.getOrderNo());
        order.setOrderStatus(OrderStatus.UNPAID);
        order.setProcessStatus(ProcessStatus.UNPAID);
        order.setCouponId(orderSubmitVo.getCouponId());
        order.setLeaderId(orderSubmitVo.getLeaderId());
        order.setLeaderName(leaderAddressVo.getLeaderName());
        order.setLeaderPhone(leaderAddressVo.getLeaderPhone());
        order.setTakeName(leaderAddressVo.getTakeName());
        order.setReceiverName(orderSubmitVo.getReceiverName());
        order.setReceiverPhone(orderSubmitVo.getReceiverPhone());
        order.setReceiverProvince(leaderAddressVo.getProvince());
        order.setReceiverCity(leaderAddressVo.getCity());
        order.setReceiverDistrict(leaderAddressVo.getDistrict());
        order.setReceiverAddress(leaderAddressVo.getDetailAddress());
        order.setWareId(cartInfoList.get(0).getWareId());

        //计算订单金额
        BigDecimal originalTotalAmount = this.computeTotalAmount(cartInfoList);
        BigDecimal activityAmount = activitySplitAmountMap.get("activity:total");
        if(null == activityAmount) activityAmount = new BigDecimal(0);
        BigDecimal couponAmount = couponInfoSplitAmountMap.get("coupon:total");
        if(null == couponAmount) couponAmount = new BigDecimal(0);
        BigDecimal totalAmount = originalTotalAmount.subtract(activityAmount).subtract(couponAmount);
        //计算订单金额
        order.setOriginalTotalAmount(originalTotalAmount);
        order.setActivityAmount(activityAmount);
        order.setCouponAmount(couponAmount);
        order.setTotalAmount(totalAmount);

        //计算团长佣金
        BigDecimal profitRate = new BigDecimal(0);//orderSetService.getProfitRate();
        BigDecimal commissionAmount = order.getTotalAmount().multiply(profitRate);
        order.setCommissionAmount(commissionAmount);

        baseMapper.insert(order);

        //保存订单项
        for(OrderItem orderItem : orderItemList) {
            orderItem.setOrderId(order.getId());
        }
        orderItemService.saveBatch(orderItemList);

        //更新优惠券使用状态
        if(null != order.getCouponId()) {
            activityFeignClient.updateCouponInfoUseStatus(order.getCouponId(), userId, order.getId());
        }

        //下单成功，记录用户商品购买个数
        String orderSkuKey = RedisConst.ORDER_SKU_MAP + orderSubmitVo.getUserId();
        BoundHashOperations<String, String, Integer> hashOperations = redisTemplate.boundHashOps(orderSkuKey);
        cartInfoList.forEach(cartInfo -> {
            if(hashOperations.hasKey(cartInfo.getSkuId().toString())) {
                Integer orderSkuNum = hashOperations.get(cartInfo.getSkuId().toString()) + cartInfo.getSkuNum();
                hashOperations.put(cartInfo.getSkuId().toString(), orderSkuNum);
            }
        });
        redisTemplate.expire(orderSkuKey, DateUtil.getCurrentExpireTimes(), TimeUnit.SECONDS);

        //发送消息
        return order.getId();
    }

    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal(0);
        for (CartInfo cartInfo : cartInfoList) {
            BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
            total = total.add(itemTotal);
        }
        return total;
    }

    /**
     * 计算购物项分摊的优惠减少金额
     * 打折：按折扣分担
     * 现金：按比例分摊
     * @param cartInfoParamList
     * @return
     */
    private Map<String, BigDecimal> computeActivitySplitAmount(List<CartInfo> cartInfoParamList) {
        Map<String, BigDecimal> activitySplitAmountMap = new HashMap<>();

        //促销活动相关信息
        List<CartInfoVo> cartInfoVoList = activityFeignClient.findCartActivityList(cartInfoParamList);

        //活动总金额
        BigDecimal activityReduceAmount = new BigDecimal(0);
        if(!CollectionUtils.isEmpty(cartInfoVoList)) {
            for(CartInfoVo cartInfoVo : cartInfoVoList) {
                ActivityRule activityRule = cartInfoVo.getActivityRule();
                List<CartInfo> cartInfoList = cartInfoVo.getCartInfoList();
                if(null != activityRule) {
                    //优惠金额， 按比例分摊
                    BigDecimal reduceAmount = activityRule.getReduceAmount();
                    activityReduceAmount = activityReduceAmount.add(reduceAmount);
                    if(cartInfoList.size() == 1) {
                        activitySplitAmountMap.put("activity:"+cartInfoList.get(0).getSkuId(), reduceAmount);
                    } else {
                        //总金额
                        BigDecimal originalTotalAmount = new BigDecimal(0);
                        for(CartInfo cartInfo : cartInfoList) {
                            BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                            originalTotalAmount = originalTotalAmount.add(skuTotalAmount);
                        }
                        //记录除最后一项是所有分摊金额， 最后一项=总的 - skuPartReduceAmount
                        BigDecimal skuPartReduceAmount = new BigDecimal(0);
                        if (activityRule.getActivityType() == ActivityType.FULL_REDUCTION) {
                            for(int i=0, len=cartInfoList.size(); i<len; i++) {
                                CartInfo cartInfo = cartInfoList.get(i);
                                if(i < len -1) {
                                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                                    //sku分摊金额
                                    BigDecimal skuReduceAmount = skuTotalAmount.divide(originalTotalAmount, 2, RoundingMode.HALF_UP).multiply(reduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);

                                    skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                                } else {
                                    BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);
                                }
                            }
                        } else {
                            for(int i=0, len=cartInfoList.size(); i<len; i++) {
                                CartInfo cartInfo = cartInfoList.get(i);
                                if(i < len -1) {
                                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));

                                    //sku分摊金额
                                    BigDecimal skuDiscountTotalAmount = skuTotalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                                    BigDecimal skuReduceAmount = skuTotalAmount.subtract(skuDiscountTotalAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);

                                    skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                                } else {
                                    BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);
                                }
                            }
                        }
                    }
                }
            }
        }
        activitySplitAmountMap.put("activity:total", activityReduceAmount);
        return activitySplitAmountMap;
    }

    private Map<String, BigDecimal> computeCouponInfoSplitAmount(List<CartInfo> cartInfoList, Long couponId) {
        Map<String, BigDecimal> couponInfoSplitAmountMap = new HashMap<>();

        if(null == couponId) return couponInfoSplitAmountMap;
        CouponInfo couponInfo = activityFeignClient.findRangeSkuIdList(cartInfoList, couponId);

        if(null != couponInfo) {
            //sku对应的订单明细
            Map<Long, CartInfo> skuIdToCartInfoMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                skuIdToCartInfoMap.put(cartInfo.getSkuId(), cartInfo);
            }
            //优惠券对应的skuId列表
            List<Long> skuIdList = couponInfo.getSkuIdList();
            if(CollectionUtils.isEmpty(skuIdList)) {
                return couponInfoSplitAmountMap;
            }
            //优惠券优化总金额
            BigDecimal reduceAmount = couponInfo.getAmount();
            if(skuIdList.size() == 1) {
                //sku的优化金额
                couponInfoSplitAmountMap.put("coupon:"+skuIdToCartInfoMap.get(skuIdList.get(0)).getSkuId(), reduceAmount);
            } else {
                //总金额
                BigDecimal originalTotalAmount = new BigDecimal(0);
                for (Long skuId : skuIdList) {
                    CartInfo cartInfo = skuIdToCartInfoMap.get(skuId);
                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                    originalTotalAmount = originalTotalAmount.add(skuTotalAmount);
                }
                //记录除最后一项是所有分摊金额， 最后一项=总的 - skuPartReduceAmount
                BigDecimal skuPartReduceAmount = new BigDecimal(0);
                if (couponInfo.getCouponType() == CouponType.CASH || couponInfo.getCouponType() == CouponType.FULL_REDUCTION) {
                    for(int i=0, len=skuIdList.size(); i<len; i++) {
                        CartInfo cartInfo = skuIdToCartInfoMap.get(skuIdList.get(i));
                        if(i < len -1) {
                            BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                            //sku分摊金额
                            BigDecimal skuReduceAmount = skuTotalAmount.divide(originalTotalAmount, 2, RoundingMode.HALF_UP).multiply(reduceAmount);
                            couponInfoSplitAmountMap.put("coupon:"+cartInfo.getSkuId(), skuReduceAmount);

                            skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                        } else {
                            BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                            couponInfoSplitAmountMap.put("coupon:"+cartInfo.getSkuId(), skuReduceAmount);
                        }
                    }
                }
            }
            couponInfoSplitAmountMap.put("coupon:total", couponInfo.getAmount());
        }
        return couponInfoSplitAmountMap;
    }

    //订单详情
    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        orderInfo.getParam().put("orderStatusName", orderInfo.getOrderStatus().getComment());
        List<OrderItem> orderItemList = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderInfo.getId()));
        orderInfo.setOrderItemList(orderItemList);
        return orderInfo;
    }

    @Override
    public OrderInfo getOrderInfoByOrderNo(String orderNo) {
        OrderInfo orderInfo = baseMapper.selectOne(new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
        return orderInfo;
    }

    @Override
    public void orderPay(String orderNo) {
        OrderInfo orderInfo = this.getOrderInfoByOrderNo(orderNo);
        if(null == orderInfo || orderInfo.getOrderStatus() != OrderStatus.UNPAID) return;

        //更改状态
        this.updateOrderStatus(orderInfo.getId(),ProcessStatus.WAITING_DELEVER);

        //扣减库存
        rabbitService.sendMessage(MqConst.EXCHANGE_ORDER_DIRECT, MqConst.ROUTING_MINUS_STOCK, orderNo);
    }

    private void updateOrderStatus(Long orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        if(processStatus == ProcessStatus.WAITING_DELEVER) {
            orderInfo.setPaymentTime(new Date());
        } else if(processStatus == ProcessStatus.WAITING_LEADER_TAKE) {
            orderInfo.setDeliveryTime(new Date());
        } else if(processStatus == ProcessStatus.WAITING_USER_TAKE) {
            orderInfo.setTakeTime(new Date());
        }
        baseMapper.updateById(orderInfo);
    }
}




