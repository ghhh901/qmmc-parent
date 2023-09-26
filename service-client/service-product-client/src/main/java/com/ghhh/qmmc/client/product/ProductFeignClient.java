package com.ghhh.qmmc.client.product;

import com.ghhh.qmmc.model.product.Category;
import com.ghhh.qmmc.model.product.SkuInfo;
import com.ghhh.qmmc.vo.product.SkuInfoVo;
import com.ghhh.qmmc.vo.product.SkuStockLockVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "service-product")
public interface ProductFeignClient {

    @GetMapping("api/product/inner/getCategory/{categoryId}")
    public Category getCategory(@PathVariable Long categoryId);

    @GetMapping("api/product/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId);

    @PostMapping("api/product/inner/findSkuInfoList")
    public List<SkuInfo> findSkuInfoList(@RequestBody List<Long> skuIds);

    @GetMapping("api/product/inner/findSkuInfoByKeyword/{keyword}")
    public List<SkuInfo> findSkuInfoByKeyword(@PathVariable String keyword);

    @PostMapping("api/product/inner/findCategoryList")
    public List<Category> findCategoryList(@RequestBody List<Long> categoryIdList);

    @GetMapping("api/product/inner/findAllCategoryList")
    public List<Category> findAllCategoryList();

    @GetMapping("api/product/inner/findNewPersonSkuInfoList")
    public List<SkuInfo> findNewPersonSkuInfoList();

    @GetMapping("api/product/inner/getSkuInfoVo/{skuId}")
    public SkuInfoVo getSkuInfoVo(@PathVariable("skuId") Long skuId);

    @PostMapping("api/product/inner/checkAndLock/{orderNo}")
    public Boolean checkAndLock(@RequestBody List<SkuStockLockVo> skuStockLockVoList, @PathVariable String orderNo);
}
