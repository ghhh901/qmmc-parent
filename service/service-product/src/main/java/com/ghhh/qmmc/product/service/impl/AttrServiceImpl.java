package com.ghhh.qmmc.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghhh.qmmc.model.product.Attr;
import com.ghhh.qmmc.product.service.AttrService;
import com.ghhh.qmmc.product.mapper.AttrMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class AttrServiceImpl extends ServiceImpl<AttrMapper, Attr>
    implements AttrService{

    @Override
    public List<Attr> findByAttrGroupId(Long attrGroupId) {
        LambdaQueryWrapper<Attr> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attr::getAttrGroupId,attrGroupId);
        List<Attr> list = baseMapper.selectList(wrapper);
        return list;
    }
}




