package com.ghhh.qmmc.service.impl;

import com.ghhh.qmmc.activity.client.ActivityFeignClient;
import com.ghhh.qmmc.client.product.ProductFeignClient;
import com.ghhh.qmmc.client.search.SearchFeignClient;
import com.ghhh.qmmc.service.ItemApiService;
import com.ghhh.qmmc.vo.product.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemApiServiceImpl implements ItemApiService {

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ActivityFeignClient activityFeignClient;

    @Autowired
    private SearchFeignClient searchFeignClient;

    @Override
    public Map<String, Object> item(Long skuId,Long userId) {

        Map<String,Object> result = new HashMap<>();

        //1.通过skuId查询SkuInfo
        CompletableFuture<SkuInfoVo> skuInfoVoCompletableFuture =
                CompletableFuture.supplyAsync(() ->{
            SkuInfoVo skuInfoVo = productFeignClient.getSkuInfoVo(skuId);
            result.put("skuInfoVo",skuInfoVo);
            return skuInfoVo;
        },threadPoolExecutor);

        //2.获取优惠劵信息
        CompletableFuture<Void> activityCompletableFuture =
                CompletableFuture.runAsync(() ->{
                    Map<String,Object> activityAndCouponMap = activityFeignClient.findActivityAndCoupon(skuId,userId);
                    result.putAll(activityAndCouponMap);
                },threadPoolExecutor);

        //3.热销产品
        CompletableFuture<Void> hotCompletableFuture =
                CompletableFuture.runAsync(() -> {
                    searchFeignClient.incrHotScore(skuId);
                },threadPoolExecutor);

        CompletableFuture.allOf(
                skuInfoVoCompletableFuture,
                activityCompletableFuture,
                hotCompletableFuture
        ).join();
        return result;
    }
}
