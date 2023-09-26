package com.ghhh.qmmc.search.controller;

import com.ghhh.qmmc.model.search.SkuEs;
import com.ghhh.qmmc.result.Result;
import com.ghhh.qmmc.search.service.SkuApiService;
import com.ghhh.qmmc.vo.search.SkuEsQueryVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/search/sku")
public class SkuApiController {

    @Autowired
    private SkuApiService skuApiService;

    @ApiOperation("上架商品")
    @GetMapping("inner/upperSku/{skuId}")
    public Result upperSku(@PathVariable("skuId") Long skuId){
        skuApiService.upperSku(skuId);
        return Result.ok(null);
    }

    @ApiOperation("下架商品")
    @GetMapping("inner/lowerSku/{skuId}")
    public Result lowerSku(@PathVariable("skuId") Long skuId){
        skuApiService.lowerSku(skuId);
        return Result.ok(null);
    }

    @ApiOperation(value = "获取爆品商品")
    @GetMapping("inner/findHotSkuList")
    public List<SkuEs> findHotSkuList() {
        return skuApiService.findHotSkuList();
    }

    @ApiOperation(value = "搜索商品")
    @GetMapping("{page}/{limit}")
    public Result list(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Integer page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Integer limit,
            @ApiParam(name = "searchParamVo", value = "查询对象", required = false)
                    SkuEsQueryVo searchParamVo) {

        Pageable pageable = PageRequest.of(page-1, limit);
        Page<SkuEs> pageModel =  skuApiService.search(pageable, searchParamVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "更新商品incrHotScore")
    @GetMapping("inner/incrHotScore/{skuId}")
    public Boolean incrHotScore(@PathVariable("skuId") Long skuId) {
        // 调用服务层
        skuApiService.incrHotScore(skuId);
        return true;
    }
}
