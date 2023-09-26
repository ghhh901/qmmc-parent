package com.ghhh.qmmc.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghhh.qmmc.model.order.OrderInfo;
import com.ghhh.qmmc.vo.order.OrderConfirmVo;
import com.ghhh.qmmc.vo.order.OrderSubmitVo;

/**
 *
 */
public interface OrderInfoService extends IService<OrderInfo> {
    //确认订单
    OrderConfirmVo confirmOrder();

    //生成订单
    Long submitOrder(OrderSubmitVo orderParamVo);

    //订单详情
    OrderInfo getOrderInfoById(Long orderId);

    OrderInfo getOrderInfoByOrderNo(String orderNo);

    void orderPay(String orderNo);

}
