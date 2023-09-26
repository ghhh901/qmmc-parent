package com.ghhh.qmmc.sys.controller;

import com.ghhh.qmmc.model.sys.Region;
import com.ghhh.qmmc.result.Result;
import com.ghhh.qmmc.sys.service.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@CrossOrigin
@RequestMapping("/admin/sys/region")
@Api(tags = "区域")
public class RegionController {

    @Autowired
    private RegionService regionService;

//    url: `${api_name}/findRegionByKeyword/${keyword}`,
//    method: 'get'
    @ApiOperation("根据区域关键字查询区域")
    @GetMapping("/findRegionByKeyword/{keyword}")
    public Result findRegion(@PathVariable("keyword") String keyword){
        List<Region> list = regionService.getRegionByKeyword(keyword);
        return Result.ok(list);
    }
}
