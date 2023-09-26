package com.ghhh.qmmc.search.service.impl;

import com.ghhh.qmmc.activity.client.ActivityFeignClient;
import com.ghhh.qmmc.client.product.ProductFeignClient;
import com.ghhh.qmmc.common.security.AuthContextHolder;
import com.ghhh.qmmc.enums.SkuType;
import com.ghhh.qmmc.model.product.Category;
import com.ghhh.qmmc.model.product.SkuInfo;
import com.ghhh.qmmc.model.search.SkuEs;
import com.ghhh.qmmc.search.repository.SkuRepository;
import com.ghhh.qmmc.search.service.SkuApiService;
import com.ghhh.qmmc.vo.search.SkuEsQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SkuApiServiceImpl implements SkuApiService {

    @Autowired
    private SkuRepository skuRepository;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ActivityFeignClient activityFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void upperSku(Long skuId) {

        SkuEs skuEs = new SkuEs();
        //1.查SkuInfo的基本信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if(skuInfo == null){
            return;
        }
        //2.查询分类
        Category category = productFeignClient.getCategory(skuInfo.getCategoryId());

        if (category != null) {
            skuEs.setCategoryId(category.getId());
            skuEs.setCategoryName(category.getName());
        }
        //set
        skuEs.setId(skuInfo.getId());
        skuEs.setKeyword(skuInfo.getSkuName()+","+skuEs.getCategoryName());
        skuEs.setWareId(skuInfo.getWareId());
        skuEs.setIsNewPerson(skuInfo.getIsNewPerson());
        skuEs.setImgUrl(skuInfo.getImgUrl());
        skuEs.setTitle(skuInfo.getSkuName());
        if(skuInfo.getSkuType() == SkuType.COMMON.getCode()) {
            skuEs.setSkuType(0);
            skuEs.setPrice(skuInfo.getPrice().doubleValue());
            skuEs.setStock(skuInfo.getStock());
            skuEs.setSale(skuInfo.getSale());
            skuEs.setPerLimit(skuInfo.getPerLimit());
        }else {
            //TODO 待完善-秒杀商品
        }

        SkuEs save = skuRepository.save(skuEs);
    }

    @Override
    public void lowerSku(Long skuId) {
        skuRepository.deleteById(skuId);
    }

    @Override
    public List<SkuEs> findHotSkuList() {
        Pageable pageable = PageRequest.of(0, 10);
        return skuRepository.findByOrderByHotScoreDesc(pageable).getContent();
    }

    @Override
    public Page<SkuEs> search(Pageable pageable, SkuEsQueryVo skuEsQueryVo) {
        skuEsQueryVo.setWareId(AuthContextHolder.getWareId());
        Page<SkuEs> page = null;
        if(StringUtils.isEmpty(skuEsQueryVo.getKeyword())) {
            page = skuRepository.findByCategoryIdAndWareId(skuEsQueryVo.getCategoryId(), skuEsQueryVo.getWareId(), pageable);
        } else {
            page = skuRepository.findByKeywordAndWareId(skuEsQueryVo.getKeyword(), skuEsQueryVo.getWareId(), pageable);
        }

        List<SkuEs>  skuEsList =  page.getContent();
        //获取sku对应的促销活动标签
        if(!CollectionUtils.isEmpty(skuEsList)) {
            List<Long> skuIdList = skuEsList.stream().map(sku -> sku.getId()).collect(Collectors.toList());
           Map<Long, List<String>> skuIdToRuleListMap = activityFeignClient.findActivity(skuIdList);

            if(null != skuIdToRuleListMap) {
                skuEsList.forEach(skuEs -> {
                    skuEs.setRuleList(skuIdToRuleListMap.get(skuEs.getId()));
                });
            }
        }
        return page;
    }

    @Override
    public void incrHotScore(Long skuId) {
        //1.在redis用zset中score做加一
        String key = "hotScore";
        Double aDouble = redisTemplate.opsForZSet().incrementScore(key, "skuid:" + skuId, 1);
        //2.用一定规则加入es中
        if (aDouble % 10 ==0){
            Optional<SkuEs> optional = skuRepository.findById(skuId);
            SkuEs skuEs = optional.get();
            skuEs.setHotScore(Math.round(aDouble));
            skuRepository.save(skuEs);
        }
    }
}
