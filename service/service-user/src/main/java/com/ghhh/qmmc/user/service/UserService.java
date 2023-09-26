package com.ghhh.qmmc.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghhh.qmmc.enums.user.User;
import com.ghhh.qmmc.vo.user.LeaderAddressVo;
import com.ghhh.qmmc.vo.user.UserLoginVo;

public interface UserService extends IService<User> {
    User selectOpenId(String openId);

    LeaderAddressVo getLeaderAddressVoByUserId(Long id);

    UserLoginVo getUserLoginVo(Long id);
}
