package com.ghhh.qmmc.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ghhh.qmmc.model.activity.ActivityInfo;
import com.ghhh.qmmc.model.activity.ActivityRule;
import com.ghhh.qmmc.model.activity.ActivitySku;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityInfoMapper extends BaseMapper<ActivityInfo> {
    List<Long> selectExistSkuIdList(List<Long> skuIdList);

    List<ActivityRule> selectActivityRuleList(@Param("skuId") Long skuId);

    List<ActivitySku> selectCartActivityList(@Param("skuIdList") List<Long> skuIdList);
}
