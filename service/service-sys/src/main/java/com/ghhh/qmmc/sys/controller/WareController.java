package com.ghhh.qmmc.sys.controller;

import com.ghhh.qmmc.model.sys.Ware;
import com.ghhh.qmmc.result.Result;
import com.ghhh.qmmc.sys.service.WareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/sys/ware")
@Api(tags = "仓库")
public class WareController {

    @Autowired
    private WareService wareService;
//    findAllList() {
////        return request({
////                url: `${api_name}/findAllList`,
////        method: 'get'
////    })
    @ApiOperation("仓库列表")
    @GetMapping("/findAllList")
    public Result findAllList(){
        List<Ware> list = wareService.list();
        return Result.ok(list);
    }
}
