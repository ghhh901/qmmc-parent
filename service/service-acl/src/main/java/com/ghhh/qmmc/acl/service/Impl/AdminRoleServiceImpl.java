package com.ghhh.qmmc.acl.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghhh.qmmc.acl.mapper.AdminRoleMapper;
import com.ghhh.qmmc.acl.service.AdminRoleService;
import com.ghhh.qmmc.acl.service.AdminService;
import com.ghhh.qmmc.model.acl.AdminRole;
import org.springframework.stereotype.Service;

@Service
public class AdminRoleServiceImpl extends ServiceImpl<AdminRoleMapper, AdminRole> implements AdminRoleService {
}
