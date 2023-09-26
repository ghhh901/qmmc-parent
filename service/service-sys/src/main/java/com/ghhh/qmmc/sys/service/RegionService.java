package com.ghhh.qmmc.sys.service;

import com.ghhh.qmmc.model.sys.Region;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 */
public interface RegionService extends IService<Region> {

    List<Region> getRegionByKeyword(String keyword);
}
