package com.ghhh.qmmc.acl.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghhh.qmmc.acl.mapper.RoleMapper;
import com.ghhh.qmmc.acl.service.AdminRoleService;
import com.ghhh.qmmc.acl.service.RoleService;
import com.ghhh.qmmc.model.acl.Admin;
import com.ghhh.qmmc.model.acl.AdminRole;
import com.ghhh.qmmc.model.acl.Role;
import com.ghhh.qmmc.vo.acl.RoleQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private AdminRoleService adminRoleService;

    @Override
    public IPage<Role> selectPage(Page<Role> rolePage, RoleQueryVo roleQueryVo) {
        String roleName = roleQueryVo.getRoleName();
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(roleName)){
            wrapper.like(Role::getRoleName,roleName);
        }
        IPage<Role> rolePage1 = baseMapper.selectPage(rolePage, wrapper);
        return rolePage1;
    }

    //获取某个用户的所有角色，和获取所有角色
    @Override
    public Map<String, Object> getAllAdminRole(Long adminId) {
        //获取所有的角色
        List<Role> allRolesList = baseMapper.selectList(null);
        //获取adminid所对应的角色
        LambdaQueryWrapper<AdminRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminRole::getAdminId,adminId);
        List<AdminRole> adminRoleList = adminRoleService.list(wrapper);
        //把用户角色集合转化为只有角色id的集合
        List<Long> longList = new ArrayList<>();
        for (AdminRole adminRole : adminRoleList) {
            longList.add(adminRole.getRoleId());
        }
        //所有角色中包含有id的集合存放到assignRoles集合中
        ArrayList<Role> assignRoles = new ArrayList<>();
        for (Role role:allRolesList) {
            if (longList.contains(role.getId())){
                assignRoles.add(role);
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("allRolesList",allRolesList);
        map.put("assignRoles",assignRoles);
        return map;
    }

    //为用户分配角色
    @Override
    public void addAdminRole(Long adminId, Long[] roleIds) {
        //1.删除用户角色
        LambdaQueryWrapper<AdminRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminRole::getAdminId,adminId);
        adminRoleService.remove(wrapper);
        //2.添加用户角色
        ArrayList<AdminRole> list = new ArrayList<>();
        for (Long roleId:roleIds) {
            AdminRole adminRole = new AdminRole();
            adminRole.setAdminId(adminId);
            adminRole.setRoleId(roleId);
            list.add(adminRole);
        }
        adminRoleService.saveBatch(list);
    }




}
