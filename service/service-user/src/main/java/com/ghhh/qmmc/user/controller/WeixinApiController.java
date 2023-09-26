package com.ghhh.qmmc.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.ghhh.qmmc.common.JwtHelper;
import com.ghhh.qmmc.common.constant.RedisConst;
import com.ghhh.qmmc.common.security.AuthContextHolder;
import com.ghhh.qmmc.enums.UserType;
import com.ghhh.qmmc.enums.user.User;
import com.ghhh.qmmc.exception.QmmcException;
import com.ghhh.qmmc.result.Result;
import com.ghhh.qmmc.result.ResultCodeEnum;
import com.ghhh.qmmc.user.mapper.LeaderMapper;
import com.ghhh.qmmc.user.mapper.UserDeliveryMapper;
import com.ghhh.qmmc.user.service.UserService;
import com.ghhh.qmmc.user.utils.ConstantPropertiesUtil;
import com.ghhh.qmmc.user.utils.HttpClientUtils;
import com.ghhh.qmmc.vo.user.LeaderAddressVo;
import com.ghhh.qmmc.vo.user.UserLoginVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user/weixin")
public class WeixinApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/wxLogin/{code}")
    public Result callback(@PathVariable String code){
        //1.获取授权临时票据code
        System.out.println("微信授权服务器回调。。。。。。"+code);
        //2.使用code和appid以及appscrect换取access_token
        StringBuffer url = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/jscode2session")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&js_code=%s")
                .append("&grant_type=authorization_code");
        String tokenUrl = String.format(url.toString(),
                ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);

        String result = null;
        try {
            result = HttpClientUtils.get(tokenUrl);
        } catch (Exception e) {
            throw new QmmcException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        System.out.println("使用code换取的access_token结果 = " + result);
        JSONObject resultJson = JSONObject.parseObject(result);
        if(resultJson.getString("errcode") != null){
            throw new QmmcException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        String accessToken = resultJson.getString("session_key");
        String openId = resultJson.getString("openid");

        //判断是否是第一次访问，如果是添加到数据库中
        User user = userService.selectOpenId(openId);
        if (user == null){
            user = new User();
            user.setOpenId(openId);
            user.setNickName(openId);
            user.setPhotoUrl("");
            user.setUserType(UserType.USER);
            user.setIsNew(0);
            userService.save(user);
        }
        //查询团长等信息
        LeaderAddressVo leaderAddressVo = userService.getLeaderAddressVoByUserId(user.getId());
        //使用JWT工具把userId和userName生成token字符串
        String token = JwtHelper.createToken(user.getId(),user.getNickName());
        //获取当前用户信息，存放到redis中
        UserLoginVo userLoginVo = userService.getUserLoginVo(user.getId());
        System.out.println(userLoginVo.toString());
        redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + user.getId(), userLoginVo, RedisConst.USERKEY_TIMEOUT, TimeUnit.DAYS);
        Map<String, Object> map = new HashMap<>();
        map.put("user",user);
        map.put("leaderAddressVo",leaderAddressVo);
        map.put("token",token);

        return Result.ok(map);
    }

    @PostMapping("/auth/updateUser")
    @ApiOperation(value = "更新用户昵称与头像")
    public Result updateUser(@RequestBody User user) {
        User user1 = userService.getById(AuthContextHolder.getUserId());
        //把昵称更新为微信用户
        user1.setNickName(user.getNickName().replaceAll("[ue000-uefff]", "*"));
        user1.setPhotoUrl(user.getPhotoUrl());
        userService.updateById(user1);
        return Result.ok(null);
    }
}
