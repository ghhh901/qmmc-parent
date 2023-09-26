package com.ghhh.qmmc.cart.client;

import com.ghhh.qmmc.model.order.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "service-cart")
public interface CartFeignClient {

    @GetMapping("/api/cart/inner/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") Long userId);
}
