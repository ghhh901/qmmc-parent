package com.ghhh.qmmc.client.search;

import com.ghhh.qmmc.model.search.SkuEs;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "service-search")
public interface SearchFeignClient {
    @GetMapping("api/search/sku/inner/findHotSkuList")
    public List<SkuEs> findHotSkuList();

    @GetMapping("api/search/sku/inner/incrHotScore/{skuId}")
    public Boolean incrHotScore(@PathVariable("skuId") Long skuId);
}
