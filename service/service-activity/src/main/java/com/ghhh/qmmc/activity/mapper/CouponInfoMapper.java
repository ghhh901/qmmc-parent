package com.ghhh.qmmc.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ghhh.qmmc.model.activity.CouponInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponInfoMapper extends BaseMapper<CouponInfo> {
    List<CouponInfo> selectCouponInfoList(@Param("skuId") Long skuId, @Param("categoryId") Long categoryId, @Param("userId") Long userId);

    List<CouponInfo> selectCartCouponInfoList(@Param("userId") Long userId);
}
