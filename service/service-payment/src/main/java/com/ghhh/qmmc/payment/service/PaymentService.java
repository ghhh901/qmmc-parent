package com.ghhh.qmmc.payment.service;

import com.ghhh.qmmc.enums.PaymentType;
import com.ghhh.qmmc.model.order.PaymentInfo;

import java.util.Map;

public interface PaymentService {

    PaymentInfo getPaymentInfo(String orderNo, PaymentType weixin);

    PaymentInfo savePaymentInfo(String orderNo, PaymentType weixin);

    void paySuccess(String out_trade_no, PaymentType weixin, Map<String, String> resultMap);
}
