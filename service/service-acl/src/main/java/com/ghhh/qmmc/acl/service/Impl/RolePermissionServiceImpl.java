package com.ghhh.qmmc.acl.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghhh.qmmc.acl.mapper.RolePermissionMapper;
import com.ghhh.qmmc.acl.service.RolePermissionService;
import com.ghhh.qmmc.model.acl.RolePermission;
import org.springframework.stereotype.Service;

@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {
}
