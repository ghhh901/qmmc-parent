package com.ghhh.qmmc.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface HomeApiService{
    Map<String,Object> home(Long userId);
}
