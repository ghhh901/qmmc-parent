package com.ghhh.qmmc.acl.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghhh.qmmc.acl.mapper.AdminMapper;
import com.ghhh.qmmc.acl.service.AdminService;
import com.ghhh.qmmc.model.acl.Admin;
import com.ghhh.qmmc.vo.acl.AdminQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Override
    public IPage<Admin> selectAdminPage(Page<Admin> adminPage, AdminQueryVo adminQueryVo) {
        String name = adminQueryVo.getName();
        String username = adminQueryVo.getUsername();
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(name)){
            wrapper.like(Admin::getName,name);
        }
        if (!StringUtils.isEmpty(username)){
            wrapper.eq(Admin::getUsername,username);
        }
        Page<Admin> adminPage1 = baseMapper.selectPage(adminPage, wrapper);
        return adminPage1;
    }
}
