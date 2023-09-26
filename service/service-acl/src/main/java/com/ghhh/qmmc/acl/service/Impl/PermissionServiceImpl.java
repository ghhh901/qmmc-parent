package com.ghhh.qmmc.acl.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghhh.qmmc.acl.mapper.PermissionMapper;
import com.ghhh.qmmc.acl.service.PermissionService;
import com.ghhh.qmmc.acl.service.RolePermissionService;
import com.ghhh.qmmc.acl.service.RoleService;
import com.ghhh.qmmc.acl.util.PermissionHelper;
import com.ghhh.qmmc.model.acl.Permission;

import com.ghhh.qmmc.model.acl.Role;
import com.ghhh.qmmc.model.acl.RolePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Autowired
    private RolePermissionService rolePermissionService;

    @Override
    public List<Permission> getPermissionsList() {
        List<Permission> allPermission = baseMapper.selectList(null);
        List<Permission> permissions = PermissionHelper.bulid(allPermission);
        return permissions;
    }

    //递归删除
    @Override
    public void deletePermission(Long id) {
        ArrayList<Long> list = new ArrayList<>();
        list.add(id);
        this.queryAll(id,list);
        baseMapper.deleteBatchIds(list);
    }

    //allPermissions
//    @Override
//    public Map<String, Object> getAllRolePermission(Long roleId) {
//        Map<String,Object> map = new HashMap<>();
//        List<Permission> allPermissionsList = baseMapper.selectList(null);
//        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(RolePermission::getRoleId,roleId);
//        List<RolePermission> list = rolePermissionService.list(wrapper);
//        ArrayList<Long> longList = new ArrayList<>();
//        for (RolePermission rp: list) {
//            longList.add(rp.getPermissionId());
//        }
//        ArrayList<Object> assignPermissions = new ArrayList<>();
//        for (Permission ap : allPermissionsList) {
//            if (longList.contains(ap.getId())){
//                assignPermissions.add(ap);
//            }
//        }
//        map.put("allPermissionsList",allPermissionsList);
//        map.put("allPermissions",assignPermissions);
//        return map;
//    }

    @Override
    public List<Permission> getAllRolePermission(Long roleId) {

        List<Permission> allPermissionsList = baseMapper.selectList(null);
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId,roleId);
        List<RolePermission> list = rolePermissionService.list(wrapper);
        ArrayList<Long> longList = new ArrayList<>();
        for (RolePermission rp: list) {
            longList.add(rp.getPermissionId());
        }
        ArrayList<Permission> assignPermissions = new ArrayList<>();
        for (Permission ap : allPermissionsList) {
            if (longList.contains(ap.getId())){
                assignPermissions.add(ap);
            }
        }

        return assignPermissions;
    }

    @Override
    public void doAssignPermission(Long roleId, Long[] permissionId) {
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId,roleId);
        rolePermissionService.remove(wrapper);
        List<RolePermission> list = new ArrayList<>();
        for (Long per : permissionId) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(per);
            list.add(rolePermission);
        }
        rolePermissionService.saveBatch(list);
    }


    //查询要删除的所有id
    void queryAll(Long id,List<Long> ids){
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPid,id);
        List<Permission> list = baseMapper.selectList(wrapper);
        for (Permission per:list) {
            ids.add(per.getId());
            this.queryAll(per.getId(),ids);
        }
    }


}
