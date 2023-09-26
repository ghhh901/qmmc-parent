package com.ghhh.qmmc.user.api;

import com.ghhh.qmmc.user.service.UserService;
import com.ghhh.qmmc.vo.user.LeaderAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/leader")
public class LeaderApiController {

    @Autowired
    private UserService userService;

    @GetMapping("/inner/getUserAddressByUserId/{userId}")
    public LeaderAddressVo getLeaderAddressVoByUserId(@PathVariable Long userId){
        return userService.getLeaderAddressVoByUserId(userId);
    }

}
