package com.ghhh.qmmc.product.controller;

import com.ghhh.qmmc.model.product.Attr;
import com.ghhh.qmmc.product.service.AttrService;
import com.ghhh.qmmc.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "Attr管理", tags = "平台属性管理")
@RestController
@RequestMapping(value="/admin/product/attr")
public class AttrController {

    @Autowired
    private AttrService attrService;

    @ApiOperation(value = "获取列表")
    @GetMapping("{attrGroupId}")
    public Result index(
            @ApiParam(name = "attrGroupId", value = "分组id", required = true)
            @PathVariable Long attrGroupId) {
        List<Attr> list = attrService.findByAttrGroupId(attrGroupId);
        return Result.ok(list);
    }

    @ApiOperation(value = "获取")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        Attr attr = attrService.getById(id);
        return Result.ok(attr);
    }

    @ApiOperation(value = "新增")
    @PostMapping("save")
    public Result save(@RequestBody Attr attr) {
        attrService.save(attr);
        return Result.ok(null);
    }

    @ApiOperation(value = "修改")
    @PutMapping("update")
    public Result updateById(@RequestBody Attr attr) {
        attrService.updateById(attr);
        return Result.ok(null);
    }

    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        attrService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation(value = "根据id列表删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        attrService.removeByIds(idList);
        return Result.ok(null);
    }
}
