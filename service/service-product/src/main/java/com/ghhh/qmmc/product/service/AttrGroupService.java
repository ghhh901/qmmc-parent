package com.ghhh.qmmc.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghhh.qmmc.model.product.AttrGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghhh.qmmc.vo.product.AttrGroupQueryVo;

import java.util.List;

/**
 *
 */
public interface AttrGroupService extends IService<AttrGroup> {

    IPage<AttrGroup> getPageList(Page<AttrGroup> page1, AttrGroupQueryVo attrGroupQueryVo);

    List<AttrGroup> findAllList();

}
