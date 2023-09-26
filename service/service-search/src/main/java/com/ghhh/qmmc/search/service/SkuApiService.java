package com.ghhh.qmmc.search.service;

import com.ghhh.qmmc.model.search.SkuEs;
import com.ghhh.qmmc.vo.search.SkuEsQueryVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SkuApiService {
    void upperSku(Long skuId);

    void lowerSku(Long skuId);

    List<SkuEs> findHotSkuList();

    Page<SkuEs> search(Pageable pageable, SkuEsQueryVo searchParamVo);

    void incrHotScore(Long skuId);
}
