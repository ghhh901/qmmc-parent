package com.ghhh.qmmc.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghhh.qmmc.model.product.SkuInfo;
import com.ghhh.qmmc.product.service.SkuInfoService;
import com.ghhh.qmmc.result.Result;
import com.ghhh.qmmc.vo.product.SkuInfoQueryVo;
import com.ghhh.qmmc.vo.product.SkuInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "SkuInfo管理", tags = "商品Sku管理")
@RestController
@RequestMapping(value="/admin/product/skuInfo")
public class SkuInfoController {

    @Autowired
    private SkuInfoService skuInfoService;

//    url: `${api_name}/${page}/${limit}`,
//    method: 'get',
//    params: searchObj

    @ApiOperation(value = "获取sku分页列表")
    @GetMapping("/{page}/{limit}")
    public Result<IPage<SkuInfo>> index(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,

            @ApiParam(name = "skuInfoQueryVo", value = "查询对象", required = false)
                    SkuInfoQueryVo skuInfoQueryVo) {
        Page<SkuInfo> pageParam = new Page<>(page, limit);
        IPage<SkuInfo> pageModel = skuInfoService.selectPage(pageParam, skuInfoQueryVo);
        return Result.ok(pageModel);
    }

//    url: `${api_name}/save`,
//    method: 'post',
//    data: role
    @ApiOperation("新增")
    @PostMapping("/save")
    public Result save(@RequestBody SkuInfoVo skuInfoVo){
        skuInfoService.saveSkuInfo(skuInfoVo);
        return Result.ok(null);
    }

//    url: `${api_name}/get/${id}`,
//    method: 'get'
    @ApiOperation("根据id查询")
    @GetMapping("/get/{id}")
    public Result getSkuInfo(@PathVariable Long id){
        SkuInfoVo skuInfoVo = skuInfoService.getSkuInfoById(id);
        return Result.ok(skuInfoVo);
    }

//    url: `${api_name}/update`,
//    method: 'put',
//    data: role
    @ApiOperation("修改")
    @PutMapping("/update")
    public Result update(@RequestBody SkuInfoVo skuInfoVo){
        skuInfoService.updateSkuInfo(skuInfoVo);
        return Result.ok(null);
    }

    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        skuInfoService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation(value = "根据id列表删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        skuInfoService.removeByIds(idList);
        return Result.ok(null);
    }

    @GetMapping("check/{skuId}/{status}")
    public Result check(@PathVariable("skuId") Long skuId, @PathVariable("status") Integer status) {
        skuInfoService.check(skuId, status);
        return Result.ok(null);
    }

    /**
     * 商品上架
     * @param skuId
     * @return
     */
    @GetMapping("publish/{skuId}/{status}")
    public Result publish(@PathVariable("skuId") Long skuId,
                          @PathVariable("status") Integer status) {
        skuInfoService.publish(skuId, status);
        return Result.ok(null);
    }

    //新人专享
    @GetMapping("isNewPerson/{skuId}/{status}")
    public Result isNewPerson(@PathVariable("skuId") Long skuId,
                              @PathVariable("status") Integer status) {
        skuInfoService.isNewPerson(skuId, status);
        return Result.ok(null);
    }
}
