package com.ghhh.qmmc.acl.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghhh.qmmc.model.acl.Role;
import com.ghhh.qmmc.vo.acl.RoleQueryVo;

import java.util.Map;

public interface RoleService extends IService<Role> {

    IPage<Role> selectPage(Page<Role> rolePage, RoleQueryVo roleQueryVo);

    Map<String, Object> getAllAdminRole(Long adminId);

    void addAdminRole(Long adminId, Long[] roleIds);

}
