package com.ghhh.qmmc.acl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghhh.qmmc.acl.service.RoleService;
import com.ghhh.qmmc.model.acl.Role;
import com.ghhh.qmmc.result.Result;
import com.ghhh.qmmc.vo.acl.RoleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "角色管理")
@Slf4j
@RequestMapping("/admin/acl/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    //分页列表查询
    @ApiOperation("分页列表查询")
    @GetMapping("/{page}/{limit}")
    public Result index(@ApiParam(name = "page",value = "当前页码",required = true)
                        @PathVariable Long page,
                        @ApiParam(name = "limit",value = "每页记录数",required = true)
                        @PathVariable Long limit,
                        @ApiParam(name = "roleQueryVo",value = "查询对象",required = false)
                        RoleQueryVo roleQueryVo){
        Page<Role> rolePage = new Page<>(page,limit);
        IPage<Role> iPage = roleService.selectPage(rolePage, roleQueryVo);
        return Result.ok(iPage);
    }

    //根据id获取角色
    @ApiOperation("根据id查角色")
    @GetMapping("/get/{id}")
    public Result getId(@ApiParam(name = "id",value = "id名",required =false)
                        @PathVariable Long id){
        Role role = roleService.getById(id);
        return Result.ok(role);
    }

    //添加角色
    @ApiOperation("添加角色")
    @PostMapping("/save")
    public Result save(@RequestBody Role role){//@RequestBody 接收josn数据
        boolean is_success = roleService.save(role);
        if (is_success){
            return Result.ok(null);
        }else {
            return Result.fail(null);
        }
    }

    //修改角色
    @ApiOperation("修改角色")
    @PutMapping("/update")
    public Result update(@RequestBody Role role){
        roleService.updateById(role);
        return Result.ok(null);
    }

    //根据id删除
    @ApiOperation("根据id删除")
    @DeleteMapping("/remove/{id}")
    public Result delete(@PathVariable Long id){
        roleService.removeById(id);
        return Result.ok(null);
    }

    //批量删除
    //josn的数组对应java的集合
    @ApiOperation("批量删除")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        roleService.removeByIds(idList);
        return Result.ok(null);
    }
}
