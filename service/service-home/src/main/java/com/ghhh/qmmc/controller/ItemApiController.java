package com.ghhh.qmmc.controller;

import com.ghhh.qmmc.common.security.AuthContextHolder;
import com.ghhh.qmmc.result.Result;
import com.ghhh.qmmc.service.ItemApiService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("api/home")
@Api(tags = "商品详情")
public class ItemApiController {

    @Resource
    private ItemApiService itemApiService;

    @GetMapping("item/{id}")
    public Result index(@PathVariable Long id){
        Long userId = AuthContextHolder.getUserId();
        Map<String,Object> map = itemApiService.item(id,userId);
        return Result.ok(map);
    }
}
