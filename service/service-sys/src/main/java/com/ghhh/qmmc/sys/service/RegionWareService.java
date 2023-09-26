package com.ghhh.qmmc.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghhh.qmmc.model.sys.RegionWare;
import com.ghhh.qmmc.vo.sys.RegionWareQueryVo;

/**
 *
 */
public interface RegionWareService extends IService<RegionWare> {

    IPage<RegionWare> selectPageRegionWare(Page<RegionWare> page1, RegionWareQueryVo regionWareQueryVo);

    void saveRegionWare(RegionWare regionWare);

    void updateStatu(Long id, Integer status);
}
