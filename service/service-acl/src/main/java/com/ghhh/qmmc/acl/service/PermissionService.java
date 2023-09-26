package com.ghhh.qmmc.acl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghhh.qmmc.model.acl.Permission;

import java.util.List;
import java.util.Map;

public interface PermissionService extends IService<Permission> {
    List<Permission> getPermissionsList();

    void deletePermission(Long id);

    List<Permission> getAllRolePermission(Long roleId);

    void doAssignPermission(Long roleId, Long[] permissionId);
}
