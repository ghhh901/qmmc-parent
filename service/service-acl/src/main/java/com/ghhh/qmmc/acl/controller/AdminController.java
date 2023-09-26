package com.ghhh.qmmc.acl.controller;

import com.ghhh.qmmc.common.MD5;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghhh.qmmc.acl.service.AdminService;
import com.ghhh.qmmc.acl.service.RoleService;
import com.ghhh.qmmc.model.acl.Admin;
import com.ghhh.qmmc.result.Result;
import com.ghhh.qmmc.vo.acl.AdminQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/acl/user")
@Api(tags = "用户管理")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private RoleService roleService;

    //为用户分配角色
    @ApiOperation("为用户分配角色")
    @PostMapping("/doAssign")
    public Result assignRoles(@RequestParam Long adminId,
                              @RequestParam Long[] roleId){
        roleService.addAdminRole(adminId,roleId);
        return Result.ok(null);
    }

    //获取某个用户的所有角色，和获取所有角色
    @ApiOperation("获取所有角色")
    @GetMapping("/toAssign/{adminId}")
    public Result getAllAdminRole(@PathVariable Long adminId){
        Map<String,Object> map = roleService.getAllAdminRole(adminId);
        return Result.ok(map);
    }


    @ApiOperation("分页查询列表")
    @GetMapping("/{page}/{limit}")
    public Result getPageList(@PathVariable Long page,
                              @PathVariable Long limit,
                              AdminQueryVo adminQueryVo){
        Page<Admin> adminPage = new Page<>(page, limit);
        IPage<Admin> adminIPage = adminService.selectAdminPage(adminPage,adminQueryVo);
        return Result.ok(adminIPage);
    }

    @ApiOperation("根据ID获取用户")
    @GetMapping("/get/{id}")
    public Result getById(@PathVariable Long id){
        Admin admin = adminService.getById(id);
        return Result.ok(admin);
    }

    @ApiOperation("添加用户")
    @PostMapping("/save")
    public Result add(@RequestBody Admin admin){
        admin.setPassword(MD5.encrypt(admin.getPassword()));
        adminService.save(admin);
        return Result.ok(null);
    }

    @ApiOperation("修改用户")
    @PutMapping("/update")
    public Result update(@RequestBody Admin admin){
        adminService.updateById(admin);
        return Result.ok(null);
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/remove/{id}")
    public Result delete(@PathVariable Long id){
        adminService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation("批量删除")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        adminService.removeByIds(idList);
        return Result.ok(null);
    }
}
