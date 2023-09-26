package com.ghhh.qmmc.payment.service;

import java.util.Map;

public interface WeixinService {
    Map createJsapi(String orderNo);

    Map<String, String> queryPayStatus(String orderNo, String name);
}
