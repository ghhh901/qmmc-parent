package com.ghhh.qmmc.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghhh.qmmc.model.product.SkuInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghhh.qmmc.vo.product.SkuInfoQueryVo;
import com.ghhh.qmmc.vo.product.SkuInfoVo;
import com.ghhh.qmmc.vo.product.SkuStockLockVo;

import java.util.List;

/**
 *
 */
public interface SkuInfoService extends IService<SkuInfo> {

    IPage<SkuInfo> selectPage(Page<SkuInfo> pageParam, SkuInfoQueryVo skuInfoQueryVo);

    void saveSkuInfo(SkuInfoVo skuInfoVo);

    SkuInfoVo getSkuInfoById(Long id);

    void updateSkuInfo(SkuInfoVo skuInfoVo);

    void check(Long skuId, Integer status);

    void publish(Long skuId, Integer status);

    void isNewPerson(Long skuId, Integer status);

    List<SkuInfo> findSkuInfoList(List<Long> skuIds);

    List<SkuInfo> findSkuInfoByKeyword(String keyword);

    List<SkuInfo> findNewPersonList();

    SkuInfoVo getSkuInfoVo(Long skuId);

    Boolean checkAndLock(List<SkuStockLockVo> skuStockLockVoList, String orderNo);

    void minusStock(String orderNo);
}
