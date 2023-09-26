package com.ghhh.qmmc.cart.service.impl;

import com.ghhh.qmmc.cart.service.CartInfoService;
import com.ghhh.qmmc.client.product.ProductFeignClient;
import com.ghhh.qmmc.common.constant.RedisConst;
import com.ghhh.qmmc.enums.SkuType;
import com.ghhh.qmmc.exception.QmmcException;
import com.ghhh.qmmc.model.order.CartInfo;
import com.ghhh.qmmc.model.product.SkuInfo;
import com.ghhh.qmmc.result.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartInfoServiceImpl implements CartInfoService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductFeignClient productFeignClient;

    private String getCartKey(Long userId) {
        //定义key user:userId:cart
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }

    @Override
    public void addToCart(Long skuId, Long userId, Integer skuNum) {
        //1.获取userId对应的内层值
        String cartKey = getCartKey(userId);
        BoundHashOperations<String,String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        //2.判断skuId是否存在
        CartInfo cartInfo = null;
        if (hashOperations.hasKey(skuId.toString())){
            //如果购物车中有此商品
            cartInfo = hashOperations.get(skuId.toString());
            int currentSkuNum = cartInfo.getSkuNum() + skuNum;
            if (currentSkuNum < 1){
                return;
            }
            cartInfo.setSkuNum(currentSkuNum);
            cartInfo.setCurrentBuyNum(currentSkuNum);
            //大于限购个数，不能更新个数
            if(currentSkuNum >= cartInfo.getPerLimit()) {
                throw new QmmcException(ResultCodeEnum.SKU_LIMIT_ERROR);
            }
            cartInfo.setIsChecked(1);
            cartInfo.setUpdateTime(new Date());
        } else {
            cartInfo = new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            if (skuInfo == null){
                throw new QmmcException(ResultCodeEnum.DATA_ERROR);
            }
            cartInfo.setSkuId(skuId);
            cartInfo.setCategoryId(skuInfo.getCategoryId());
            cartInfo.setSkuType(skuInfo.getSkuType());
            cartInfo.setIsNewPerson(skuInfo.getIsNewPerson());
            cartInfo.setUserId(userId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setCurrentBuyNum(skuNum);
            cartInfo.setSkuType(SkuType.COMMON.getCode());
            cartInfo.setPerLimit(skuInfo.getPerLimit());
            cartInfo.setImgUrl(skuInfo.getImgUrl());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setWareId(skuInfo.getWareId());
            cartInfo.setIsChecked(1);
            cartInfo.setStatus(1);
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
        }
        //更新redis
        hashOperations.put(skuId.toString(),cartInfo);
        //设置过期时间
        this.setCartKeyExpire(cartKey);
    }

    @Override
    public void deleteCart(Long skuId, Long userId) {
        BoundHashOperations<String, String, CartInfo> boundHashOps = this.redisTemplate.boundHashOps(this.getCartKey(userId));
        //  判断购物车中是否有该商品！
        if (boundHashOps.hasKey(skuId.toString())){
            boundHashOps.delete(skuId.toString());
        }
    }

    @Override
    public void deleteAllCart(Long userId) {
        String cartKey = getCartKey(userId);
        //获取缓存对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        hashOperations.values().forEach(cartInfo -> {
            hashOperations.delete(cartInfo.getSkuId().toString());
        });
    }

    @Override
    public void batchDeleteCart(List<Long> skuIdList, Long userId) {
        String cartKey = getCartKey(userId);
        //获取缓存对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        skuIdList.forEach(skuId -> {
            hashOperations.delete(skuId.toString());
        });
    }

    @Override
    public List<CartInfo> getCartList(Long userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (StringUtils.isEmpty(userId)){
            return cartInfoList;
        }
        String cartKey = getCartKey(userId);
        BoundHashOperations hashOperations = redisTemplate.boundHashOps(cartKey);
        cartInfoList = hashOperations.values();
        if (!CollectionUtils.isEmpty(cartInfoList)){
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o2.getCreateTime().compareTo(o1.getCreateTime());
                }
            });
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(Long userId, Integer isChecked, Long skuId) {
        String cartKey = getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        CartInfo cartInfo = hashOperations.get(skuId.toString());
        if (cartInfo != null){
            cartInfo.setIsChecked(isChecked);
            hashOperations.put(skuId.toString(),cartInfo);
            this.setCartKeyExpire(cartKey);
        }
    }

    @Override
    public void checkAllCart(Long userId, Integer isChecked) {
        String cartKey = getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        List<CartInfo> cartInfos = hashOperations.values();
        cartInfos.forEach(cartInfo -> {
            cartInfo.setIsChecked(isChecked);
            hashOperations.put(cartInfo.getSkuId().toString(),cartInfo);
        });
        this.setCartKeyExpire(cartKey);
    }

    @Override
    public void batchCheckCart(List<Long> skuIdList, Long userId, Integer isChecked) {
        String cartKey = getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        skuIdList.forEach(skuId -> {
           CartInfo cartInfo =  hashOperations.get(skuId.toString());
           cartInfo.setIsChecked(isChecked);
           hashOperations.put(cartInfo.getSkuId().toString(),cartInfo);
        });
        this.setCartKeyExpire(cartKey);
    }

    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(this.getCartKey(userId));
        List<CartInfo> cartInfoList = hashOperations.values();
        List<CartInfo> infoList = cartInfoList.stream().filter(cartInfo -> {
            return cartInfo.getIsChecked().intValue() == 1;
        }).collect(Collectors.toList());
        return infoList;
    }

    @Override
    public void batchDeleteCartRQ(Long userId) {
        List<CartInfo> cartInfoList = this.getCartCheckedList(userId);
        List<Long> skuIdList = cartInfoList.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(getCartKey(userId));
        skuIdList.forEach(skuId -> {
            hashOperations.delete(skuId.toString());
        });

    }

    //  设置key 的过期时间！
    private void setCartKeyExpire(String cartKey) {
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }


}
