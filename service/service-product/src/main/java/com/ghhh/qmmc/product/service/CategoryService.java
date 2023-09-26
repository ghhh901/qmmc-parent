package com.ghhh.qmmc.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghhh.qmmc.model.product.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghhh.qmmc.vo.product.CategoryQueryVo;

import java.util.List;

/**
 *
 */
public interface CategoryService extends IService<Category> {

    IPage<Category> getPageList(Page<Category> page1, CategoryQueryVo categoryQueryVo);

    List<Category> findCategoryList(List<Long> categoryIdList);

    List<Category> findAllList();

}
