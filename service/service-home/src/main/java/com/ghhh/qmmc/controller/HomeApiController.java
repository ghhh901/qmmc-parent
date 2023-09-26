package com.ghhh.qmmc.controller;

import com.ghhh.qmmc.common.security.AuthContextHolder;
import com.ghhh.qmmc.result.Result;
import com.ghhh.qmmc.service.HomeApiService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/home")
public class HomeApiController {

    @Resource
    private HomeApiService homeApiService;

    @ApiOperation(value = "获取首页数据")
    @GetMapping("index")
    public Result index(HttpServletRequest request) {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        return Result.ok(homeApiService.home(userId));
    }
}
