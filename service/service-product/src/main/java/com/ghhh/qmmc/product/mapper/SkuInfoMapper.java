package com.ghhh.qmmc.product.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ghhh.qmmc.model.product.SkuInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

/**
 * @Entity com.ghhh.qmmc.product..SkuInfo
 */
@Component
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    //解锁
    Integer unlockStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    //查库存
    SkuInfo checkStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    //加锁
    Integer lockStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    Integer minusStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);
}




