package com.ghhh.qmmc.acl.controller;

import com.ghhh.qmmc.acl.service.PermissionService;
import com.ghhh.qmmc.acl.service.RoleService;
import com.ghhh.qmmc.model.acl.Permission;
import com.ghhh.qmmc.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/acl/permission")
@Api(tags = "菜单管理")
public class PermissionController {


    @Autowired
    private PermissionService permissionService;

    @ApiOperation("菜单列表")
    @GetMapping
    public Result getPermissionList(){
        List<Permission> list = permissionService.getPermissionsList();
        return Result.ok(list);
    }

    @ApiOperation("增加")
    @PostMapping("/save")
    public Result addPermission(@RequestBody Permission permission){
        permissionService.save(permission);
        return Result.ok(null);
    }

    @ApiOperation("修改")
    @PutMapping("/update")
    public Result updatePermission(@RequestBody Permission permission){
        permissionService.updateById(permission);
        return Result.ok(null);
    }

    @ApiOperation("递归删除")
    @DeleteMapping("/remove/{id}")
    public Result deletePermission(@PathVariable Long id){
        permissionService.deletePermission(id);
        return Result.ok(null);
    }

//    @ApiOperation("查看角色的菜单权限列表")
//    @GetMapping("/toAssign/{roleId}")
//    public Result toAssign(@PathVariable Long roleId){
//        Map<String,Object> map = permissionService.getAllRolePermission(roleId);
//        return Result.ok(map);
//    }

    @ApiOperation("查看角色的菜单权限列表")
    @GetMapping("/toAssign/{roleId}")
    public Result toAssign(@PathVariable Long roleId){
        List<Permission> list = permissionService.getAllRolePermission(roleId);
        return Result.ok(list);
    }

    @ApiOperation("给某个角色授权")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestParam Long roleId,
                           @RequestParam Long[] permissionId){
        permissionService.doAssignPermission(roleId,permissionId);
        return Result.ok(null);
    }
}
