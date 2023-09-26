package com.ghhh.qmmc.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghhh.qmmc.model.product.Category;
import com.ghhh.qmmc.product.service.CategoryService;
import com.ghhh.qmmc.product.mapper.CategoryMapper;
import com.ghhh.qmmc.vo.product.CategoryQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 *
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

    @Override
    public IPage<Category> getPageList(Page<Category> page1, CategoryQueryVo categoryQueryVo) {
        String name = categoryQueryVo.getName();
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(name)){
            wrapper.like(Category::getName,name);
        }
        IPage<Category> pageModel = baseMapper.selectPage(page1,wrapper);
        return pageModel;
    }

    @Override
    public List<Category> findCategoryList(List<Long> categoryIdList) {
        List<Category> categories = baseMapper.selectBatchIds(categoryIdList);
        return categories;
    }

    @Override
    public List<Category> findAllList() {
        List<Category> list = this.list();
        return list;
    }
}




