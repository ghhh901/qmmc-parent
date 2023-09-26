package com.ghhh.qmmc.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghhh.qmmc.common.constant.MqConst;
import com.ghhh.qmmc.common.constant.RedisConst;
import com.ghhh.qmmc.common.service.RabbitService;
import com.ghhh.qmmc.exception.QmmcException;
import com.ghhh.qmmc.model.product.SkuAttrValue;
import com.ghhh.qmmc.model.product.SkuImage;
import com.ghhh.qmmc.model.product.SkuInfo;
import com.ghhh.qmmc.model.product.SkuPoster;
import com.ghhh.qmmc.product.service.SkuAttrValueService;
import com.ghhh.qmmc.product.service.SkuImageService;
import com.ghhh.qmmc.product.service.SkuInfoService;
import com.ghhh.qmmc.product.mapper.SkuInfoMapper;
import com.ghhh.qmmc.product.service.SkuPosterService;
import com.ghhh.qmmc.result.Result;
import com.ghhh.qmmc.result.ResultCodeEnum;
import com.ghhh.qmmc.vo.product.SkuInfoQueryVo;
import com.ghhh.qmmc.vo.product.SkuInfoVo;
import com.ghhh.qmmc.vo.product.SkuStockLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 *
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo>
    implements SkuInfoService{

    @Autowired
    private SkuPosterService skuPosterService;

    @Autowired
    private SkuImageService skuImageService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SkuInfoMapper skuInfoMapper;


    @Override
    public void saveSkuInfo(SkuInfoVo skuInfoVo) {
        //1.基本信息
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo,skuInfo);
        this.save(skuInfo);
        //2.平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
        //3.商品图片
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if (!CollectionUtils.isEmpty(skuImagesList)){
            for (SkuImage skuImage: skuImagesList) {
                skuImage.setSkuId(skuInfo.getId());
            }
            skuImageService.saveBatch(skuImagesList);
        }
        //4.商品海报
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if (!CollectionUtils.isEmpty(skuPosterList)){
            for (SkuPoster skuPoster : skuPosterList) {
                skuPoster.setSkuId(skuInfo.getId());
            }
            skuPosterService.saveBatch(skuPosterList);
        }
    }

    @Override
    public SkuInfoVo getSkuInfoById(Long id) {
        SkuInfoVo skuInfoVo = new SkuInfoVo();

        SkuInfo skuInfo = this.getById(id);
        BeanUtils.copyProperties(skuInfo,skuInfoVo);

        QueryWrapper<SkuPoster> wrapper = new QueryWrapper<>();
        wrapper.eq("sku_id",id);
        List<SkuPoster> posterList = skuPosterService.list(wrapper);
        skuInfoVo.setSkuPosterList(posterList);

        QueryWrapper<SkuImage> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("sku_id",id);
        List<SkuImage> imageList = skuImageService.list(wrapper1);
        skuInfoVo.setSkuImagesList(imageList);

        QueryWrapper<SkuAttrValue> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("sku_id",id);
        List<SkuAttrValue> list = skuAttrValueService.list(wrapper2);
        skuInfoVo.setSkuAttrValueList(list);

        return skuInfoVo;
    }

    @Override
    public void updateSkuInfo(SkuInfoVo skuInfoVo) {
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo,skuInfo);
        this.updateById(skuInfo);
        Long id = skuInfoVo.getId();

        LambdaQueryWrapper<SkuPoster> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuPoster::getSkuId,id);
        skuPosterService.remove(wrapper);
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if (!CollectionUtils.isEmpty(skuPosterList)){
            for (SkuPoster skuPoster : skuPosterList) {
                skuPoster.setSkuId(skuInfo.getId());
            }
            skuPosterService.saveBatch(skuPosterList);
        }

        skuImageService.remove(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId,id));
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if (!CollectionUtils.isEmpty(skuImagesList)){
            for (SkuImage skuImage: skuImagesList) {
                skuImage.setSkuId(skuInfo.getId());
            }
            skuImageService.saveBatch(skuImagesList);
        }

        skuAttrValueService.remove(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId,id));
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
    }

    @Override
    public void check(Long skuId, Integer status) {
        // 更改发布状态
        SkuInfo skuInfoUp = new SkuInfo();
        skuInfoUp.setId(skuId);
        skuInfoUp.setCheckStatus(status);
        baseMapper.updateById(skuInfoUp);
    }

    @Override
    public void publish(Long skuId, Integer status) {
        // 更改发布状态
        if(status == 1) {
            SkuInfo skuInfo = baseMapper.selectById(skuId);
            skuInfo.setPublishStatus(status);
            baseMapper.updateById(skuInfo);
            //商品上架 后续会完善：发送mq消息更新es数据
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT, MqConst.ROUTING_GOODS_UPPER, skuId);
        } else {
            SkuInfo skuInfo = baseMapper.selectById(skuId);
            skuInfo.setPublishStatus(status);
            baseMapper.updateById(skuInfo);
            // 商品下架 后续会完善：发送mq消息更新es数据
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT, MqConst.ROUTING_GOODS_LOWER, skuId);
        }
    }

    @Override
    public void isNewPerson(Long skuId, Integer status) {
        SkuInfo skuInfoUp = new SkuInfo();
        skuInfoUp.setId(skuId);
        skuInfoUp.setIsNewPerson(status);
        baseMapper.updateById(skuInfoUp);
    }

    @Override
    public List<SkuInfo> findSkuInfoList(List<Long> skuIds) {
        List<SkuInfo> skuInfos = this.listByIds(skuIds);
        return skuInfos;

    }

    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SkuInfo::getSkuName,keyword);
        List<SkuInfo> list = this.list(wrapper);
        return list;
    }

    @Override
    public List<SkuInfo> findNewPersonList() {
        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuInfo::getPublishStatus,1);
        wrapper.eq(SkuInfo::getIsNewPerson,1);
        wrapper.orderByDesc(SkuInfo::getStock);
        Page<SkuInfo> page = new Page<>(1,3);
        Page<SkuInfo> skuInfoPage = baseMapper.selectPage(page, wrapper);
        List<SkuInfo> records = skuInfoPage.getRecords();
        return records;
    }

    @Override
    public SkuInfoVo getSkuInfoVo(Long skuId) {
        SkuInfoVo skuInfoVo = new SkuInfoVo();
        SkuInfo skuInfo = baseMapper.selectById(skuId);
        BeanUtils.copyProperties(skuInfo,skuInfoVo);

        QueryWrapper<SkuPoster> wrapper = new QueryWrapper<>();
        wrapper.eq("sku_id",skuId);
        List<SkuPoster> posterList = skuPosterService.list(wrapper);
        skuInfoVo.setSkuPosterList(posterList);

        QueryWrapper<SkuImage> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("sku_id",skuId);
        List<SkuImage> imageList = skuImageService.list(wrapper1);
        skuInfoVo.setSkuImagesList(imageList);

        QueryWrapper<SkuAttrValue> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("sku_id",skuId);
        List<SkuAttrValue> list = skuAttrValueService.list(wrapper2);
        skuInfoVo.setSkuAttrValueList(list);
        return skuInfoVo;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public Boolean checkAndLock(List<SkuStockLockVo> skuStockLockVoList,
                                String orderToken) {
        if (CollectionUtils.isEmpty(skuStockLockVoList)){
            throw new QmmcException(ResultCodeEnum.DATA_ERROR);
        }

        // 遍历所有商品，验库存并锁库存，要具备原子性
        skuStockLockVoList.forEach(skuStockLockVo -> {
            checkLock(skuStockLockVo);
        });

        // 只要有一个商品锁定失败，所有锁定成功的商品要解锁库存
        if (skuStockLockVoList.stream().anyMatch(skuStockLockVo -> !skuStockLockVo.getIsLock())) {
            // 获取所有锁定成功的商品，遍历解锁库存
            skuStockLockVoList.stream().filter(SkuStockLockVo::getIsLock).forEach(skuStockLockVo -> {
                baseMapper.unlockStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
            });
            // 响应锁定状态
            return false;
        }

        // 如果所有商品都锁定成功的情况下，需要缓存锁定信息到redis。以方便将来解锁库存 或者 减库存
        // 以orderToken作为key，以lockVos锁定信息作为value
        this.redisTemplate.opsForValue().set(RedisConst.SROCK_INFO + orderToken, skuStockLockVoList);

        // 锁定库存成功之后，定时解锁库存。
        //this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.ttl", orderToken);
        return true;
    }

    @Override
    public void minusStock(String orderNo) {
        // 获取锁定库存的缓存信息
        List<SkuStockLockVo> skuStockLockVoList = (List<SkuStockLockVo>)this.redisTemplate.opsForValue().get(RedisConst.SROCK_INFO + orderNo);
        if (CollectionUtils.isEmpty(skuStockLockVoList)){
            return ;
        }

        // 减库存
        skuStockLockVoList.forEach(skuStockLockVo -> {
            skuInfoMapper.minusStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
        });

        // 解锁库存之后，删除锁定库存的缓存。以防止重复解锁库存
        this.redisTemplate.delete(RedisConst.SROCK_INFO + orderNo);
    }

    private void checkLock(SkuStockLockVo skuStockLockVo){
        //公平锁，就是保证客户端获取锁的顺序，跟他们请求获取锁的顺序，是一样的。
        // 公平锁需要排队
        // ，谁先申请获取这把锁，
        // 谁就可以先获取到这把锁，是按照请求的先后顺序来的。
        RLock rLock = this.redissonClient
                .getFairLock(RedisConst.SKUKEY_PREFIX + skuStockLockVo.getSkuId());
        rLock.lock();

        try {
            // 验库存：查询，返回的是满足要求的库存列表
            SkuInfo skuInfo = baseMapper.checkStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
            // 如果没有一个仓库满足要求，这里就验库存失败
            if (null == skuInfo) {
                skuStockLockVo.setIsLock(false);
                return;
            }

            // 锁库存：更新
            Integer row = baseMapper.lockStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
            if (row == 1) {
                skuStockLockVo.setIsLock(true);
            }
        } finally {
            rLock.unlock();
        }
    }


    @Override
    public IPage<SkuInfo> selectPage(Page<SkuInfo> pageParam, SkuInfoQueryVo skuInfoQueryVo) {
        //获取条件值
        String keyword = skuInfoQueryVo.getKeyword();
        String skuType = skuInfoQueryVo.getSkuType();
        Long categoryId = skuInfoQueryVo.getCategoryId();
        //封装条件
        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(keyword)) {
            wrapper.like(SkuInfo::getSkuName,keyword);
        }
        if(!StringUtils.isEmpty(skuType)) {
            wrapper.eq(SkuInfo::getSkuType,skuType);
        }
        if(!StringUtils.isEmpty(categoryId)) {
            wrapper.eq(SkuInfo::getCategoryId,categoryId);
        }
        //调用方法查询
        IPage<SkuInfo> skuInfoPage = baseMapper.selectPage(pageParam, wrapper);
        return skuInfoPage;
    }


}




