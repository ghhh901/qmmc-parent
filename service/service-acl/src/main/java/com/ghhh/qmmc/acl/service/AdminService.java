package com.ghhh.qmmc.acl.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghhh.qmmc.model.acl.Admin;
import com.ghhh.qmmc.vo.acl.AdminQueryVo;

public interface AdminService extends IService<Admin> {
    IPage<Admin> selectAdminPage(Page<Admin> adminPage, AdminQueryVo adminQueryVo);
}
