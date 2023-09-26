package com.ghhh.qmmc.activity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghhh.qmmc.model.activity.ActivityInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghhh.qmmc.model.activity.ActivityRule;
import com.ghhh.qmmc.model.order.CartInfo;
import com.ghhh.qmmc.model.product.SkuInfo;
import com.ghhh.qmmc.vo.activity.ActivityRuleVo;
import com.ghhh.qmmc.vo.order.CartInfoVo;
import com.ghhh.qmmc.vo.order.OrderConfirmVo;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface ActivityInfoService extends IService<ActivityInfo> {

    IPage<ActivityInfo> getPage(Page<ActivityInfo> infoPage);

    Map<String, Object> findActivityRuleList(Long id);

    void saveActivityRule(ActivityRuleVo activityRuleVo);

    List<SkuInfo> findSkuInfoByKeyword(String keyword);

    Map<Long, List<String>> findActivity(List<Long> skuIdList);

    Map<String, Object> findActivityAndCoupon(Long skuId, Long userId);

    List<ActivityRule> findActivityRule(Long skuId);

    OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId);

    List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList);
}
