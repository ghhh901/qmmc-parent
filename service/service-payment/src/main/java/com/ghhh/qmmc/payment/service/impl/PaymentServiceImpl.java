package com.ghhh.qmmc.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ghhh.qmmc.common.constant.MqConst;
import com.ghhh.qmmc.common.service.RabbitService;
import com.ghhh.qmmc.enums.PaymentStatus;
import com.ghhh.qmmc.enums.PaymentType;
import com.ghhh.qmmc.exception.QmmcException;
import com.ghhh.qmmc.model.order.OrderInfo;
import com.ghhh.qmmc.model.order.PaymentInfo;
import com.ghhh.qmmc.order.client.OrderServiceClient;
import com.ghhh.qmmc.payment.mapper.PaymentMapper;
import com.ghhh.qmmc.payment.service.PaymentService;
import com.ghhh.qmmc.result.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public PaymentInfo getPaymentInfo(String orderNo, PaymentType weixin) {
        PaymentInfo paymentInfo = paymentMapper.selectOne(new LambdaQueryWrapper<PaymentInfo>()
                .eq(PaymentInfo::getOrderNo, orderNo)
                .eq(PaymentInfo::getPaymentType,weixin));

        return paymentInfo;
    }

    @Override
    public PaymentInfo savePaymentInfo(String orderNo, PaymentType weixin) {
        OrderInfo order = orderServiceClient.getOrderInfo(orderNo);
        if (order == null){
            throw new QmmcException(ResultCodeEnum.DATA_ERROR);
        }
        PaymentInfo paymentInfo = new PaymentInfo();

        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(PaymentType.WEIXIN);
        paymentInfo.setUserId(order.getUserId());
        paymentInfo.setOrderNo(order.getOrderNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        String subject = "test";
        paymentInfo.setSubject(subject);
        //paymentInfo.setTotalAmount(order.getTotalAmount());
        paymentInfo.setTotalAmount(new BigDecimal("0.01"));

        paymentMapper.insert(paymentInfo);
        return paymentInfo;
    }

    //修改订单状态，减库存
    @Override
    public void paySuccess(String orderNo, PaymentType weixin, Map<String, String> paramMap) {
        LambdaQueryWrapper<PaymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentInfo::getOrderNo,orderNo);
        wrapper.eq(PaymentInfo::getPaymentType,weixin);
        PaymentInfo paymentInfo = paymentMapper.selectOne(wrapper);
        if (paymentInfo.getPaymentStatus() != PaymentStatus.UNPAID){
            return;
        }
        PaymentInfo paymentInfoUpd = new PaymentInfo();
        paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
        String tradeNo = weixin == PaymentType.WEIXIN ? paramMap.get("ransaction_id") : paramMap.get("trade_no");
        paymentInfoUpd.setTradeNo(tradeNo);
        paymentInfoUpd.setCallbackTime(new Date());
        paymentInfoUpd.setCallbackContent(paramMap.toString());
        paymentMapper.update(paymentInfoUpd, new LambdaQueryWrapper<PaymentInfo>().eq(PaymentInfo::getOrderNo, orderNo));

        //Rabbit减库存
        //发送消息
        rabbitService.sendMessage(MqConst.EXCHANGE_PAY_DIRECT, MqConst.ROUTING_PAY_SUCCESS, orderNo);
    }
}
