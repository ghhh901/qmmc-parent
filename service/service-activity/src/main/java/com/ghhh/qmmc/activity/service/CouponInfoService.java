package com.ghhh.qmmc.activity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghhh.qmmc.model.activity.CouponInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghhh.qmmc.model.order.CartInfo;
import com.ghhh.qmmc.vo.activity.CouponRuleVo;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface CouponInfoService extends IService<CouponInfo> {

    IPage<CouponInfo> getPage(Page<CouponInfo> couponInfoPage);

    CouponInfo getCouponInfo(String id);

    Map<String, Object> findCouponRuleList(Long id);

    void saveCouponRule(CouponRuleVo couponRuleVo);

    List<CouponInfo> findCouponInfo(Long skuId, Long userId);

    List<CouponInfo> findCartCouponInfo(List<CartInfo> cartInfoList, Long userId);

    CouponInfo findRangeSkuIdList(List<CartInfo> cartInfoList, Long couponId);

    void updateCouponInfoUseStatus(Long couponId, Long userId, Long orderId);
}
