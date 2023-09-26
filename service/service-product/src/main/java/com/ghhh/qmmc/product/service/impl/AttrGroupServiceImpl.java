package com.ghhh.qmmc.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghhh.qmmc.model.product.AttrGroup;
import com.ghhh.qmmc.product.service.AttrGroupService;
import com.ghhh.qmmc.product.mapper.AttrGroupMapper;
import com.ghhh.qmmc.vo.product.AttrGroupQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 *
 */
@Service
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroup>
    implements AttrGroupService{

    @Override
    public IPage<AttrGroup> getPageList(Page<AttrGroup> page1, AttrGroupQueryVo attrGroupQueryVo) {
        String name = attrGroupQueryVo.getName();
        LambdaQueryWrapper<AttrGroup> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(name)){
            wrapper.like(AttrGroup::getName,name);
        }
        IPage<AttrGroup> pageModel = baseMapper.selectPage(page1,wrapper);
        return pageModel;
    }

    @Override
    public List<AttrGroup> findAllList() {
        QueryWrapper<AttrGroup> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        List<AttrGroup> list = baseMapper.selectList(wrapper);
        return list;
    }
}




