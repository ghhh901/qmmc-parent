package com.ghhh.qmmc.service.impl;

import com.ghhh.qmmc.client.product.ProductFeignClient;
import com.ghhh.qmmc.client.search.SearchFeignClient;
import com.ghhh.qmmc.client.user.UserFeignClient;
import com.ghhh.qmmc.model.product.Category;
import com.ghhh.qmmc.model.product.SkuInfo;
import com.ghhh.qmmc.model.search.SkuEs;
import com.ghhh.qmmc.service.HomeApiService;
import com.ghhh.qmmc.vo.user.LeaderAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HomeApiServiceImpl implements HomeApiService {
    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private SearchFeignClient searchFeignClient;

    @Override
    public Map<String, Object> home(Long userId) {
        Map<String,Object> result = new HashMap<>();
        //根据userId获取当前提货点信息
        LeaderAddressVo leaderAddressVo = userFeignClient.getLeaderAddressVoByUserId(userId);
        result.put("leaderAddressVo",leaderAddressVo);
        //获取所有分类
        List<Category> categoryList = productFeignClient.findAllCategoryList();
        result.put("categoryList",categoryList);
        //新人专享
        List<SkuInfo> newPersonSkuInfoList = productFeignClient.findNewPersonSkuInfoList();
        result.put("newPersonSkuInfoList",newPersonSkuInfoList);
        //获取爆品商品
        List<SkuEs> hotSkuList = searchFeignClient.findHotSkuList();
        result.put("hotSkuList",hotSkuList);

        return result;
    }
}
