package com.ghhh.qmmc.payment.controller;

import com.ghhh.qmmc.enums.PaymentType;
import com.ghhh.qmmc.payment.service.PaymentService;
import com.ghhh.qmmc.payment.service.WeixinService;
import com.ghhh.qmmc.result.Result;
import com.ghhh.qmmc.result.ResultCodeEnum;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment/weixin")
public class WeixinController {
    @Autowired
    private WeixinService weixinPayService;

    @Autowired
    private PaymentService paymentService;

    @ApiOperation(value = "下单 小程序支付")
    @GetMapping("/createJsapi/{orderNo}")
    public Result createJsapi(
            @ApiParam(name = "orderNo", value = "订单No", required = true)
            @PathVariable("orderNo") String orderNo) {
        return Result.ok(weixinPayService.createJsapi(orderNo));
    }

    @ApiOperation(value = "查询支付状态")
    @GetMapping("/queryPayStatus/{orderNo}")
    public Result queryPayStatus(
            @ApiParam(name = "orderNo", value = "订单No", required = true)
            @PathVariable("orderNo") String orderNo) {
        //调用查询接口
        Map<String, String> resultMap = weixinPayService.queryPayStatus(orderNo, PaymentType.WEIXIN.name());
        if (resultMap == null) {//出错
            return Result.build(null, ResultCodeEnum.FAIL);
        }
        if ("SUCCESS".equals(resultMap.get("trade_state"))) {//如果成功
            //更改订单状态，处理支付结果
            String out_trade_no = resultMap.get("out_trade_no");
            paymentService.paySuccess(out_trade_no, PaymentType.WEIXIN, resultMap);
            return Result.ok(null);
        }
        return Result.build(null,ResultCodeEnum.PAYMENT_WAITING);
    }

}
