package com.ghhh.qmmc.acl.controller;

import com.ghhh.qmmc.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "登录接口")
@RestController
@RequestMapping("/admin/acl/index")
public class IndexController {

    //login
    @ApiOperation("登录")
    @PostMapping("/login")
    public Result login(){
        Map<String, String> map = new HashMap<>();
        map.put("token","token-admin");
        return Result.ok(map);
    }
    //info
    @ApiOperation("内容")
    @GetMapping("/info")
    public Result info(){
        Map<String, String> map = new HashMap<>();
        map.put("name","admin");
        map.put("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        return Result.ok(map);
    }
    //logout
    @ApiOperation("登出")
    @PostMapping("/logout")
    public Result logout(){

        return Result.ok(null);
    }
}
