package com.ghhh.qmmc.order.controller;

import com.ghhh.qmmc.common.security.AuthContextHolder;
import com.ghhh.qmmc.model.order.OrderInfo;
import com.ghhh.qmmc.order.service.OrderInfoService;
import com.ghhh.qmmc.result.Result;
import com.ghhh.qmmc.vo.order.OrderConfirmVo;
import com.ghhh.qmmc.vo.order.OrderSubmitVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value="/api/order")
public class OrderInfoController {
    @Autowired
    private OrderInfoService orderService;

    @ApiOperation("确认订单")
    @GetMapping("auth/confirmOrder")
    public Result confirm() {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        return Result.ok(orderConfirmVo);
    }

    @ApiOperation("生成订单")
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderSubmitVo orderParamVo, HttpServletRequest request) {
        // 获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        return Result.ok(orderService.submitOrder(orderParamVo));
    }

    @ApiOperation("获取订单详情")
    @GetMapping("auth/getOrderInfoById/{orderId}")
    public Result getOrderInfoById(@PathVariable("orderId") Long orderId){
        return Result.ok(orderService.getOrderInfoById(orderId));
    }

    //获取orderNo信息
    @GetMapping("inner/getOrderInfo/{orderNo}")
    public OrderInfo getOrderInfo(@PathVariable("orderNo") String orderNo){
        return orderService.getOrderInfoByOrderNo(orderNo);
    }



}
