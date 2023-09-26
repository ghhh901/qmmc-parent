package com.ghhh.qmmc.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghhh.qmmc.model.sys.RegionWare;
import com.ghhh.qmmc.result.Result;
import com.ghhh.qmmc.sys.service.RegionWareService;
import com.ghhh.qmmc.vo.sys.RegionWareQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "开通区域接口")
@RequestMapping("/admin/sys/regionWare")
public class RegionWareController {

    @Autowired
    private RegionWareService regionWareService;

//    getPageList(page, limit,searchObj) {
////        return request({
////                url: `${api_name}/${page}/${limit}`,
////        method: 'get',
////                params: searchObj
////    })
////    },
    @ApiOperation("开通区域列表")
    @GetMapping("/{page}/{limit}")
    public Result getPageList(@PathVariable Long page,
                              @PathVariable Long limit,
                              RegionWareQueryVo regionWareQueryVo){
        Page<RegionWare> page1 = new Page<>(page, limit);
        IPage<RegionWare> pageModel = regionWareService.selectPageRegionWare(page1,regionWareQueryVo);
        return Result.ok(pageModel);
    }

//    save(role) {
//        return request({
//                url: `${api_name}/save`,
//        method: 'post',
//                data: role
//    })
//    },
    @ApiOperation("添加开通区域")
    @PostMapping("/save")
    public Result save(@RequestBody RegionWare regionWare){
        regionWareService.saveRegionWare(regionWare);
        return Result.ok(null);
    }

//    url: `${api_name}/remove/${id}`,
//    method: 'delete'
    @ApiOperation("删除开通区域")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable Long id){
        regionWareService.removeById(id);
        return Result.ok(null);
    }

//    url: `${api_name}/updateStatus/${id}/${status}`,
//    method: 'post'
    @ApiOperation("取消开通区域")
    @PostMapping("/updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id,
                               @PathVariable Integer status){
        regionWareService.updateStatu(id,status);
        return Result.ok(null);
    }
}
