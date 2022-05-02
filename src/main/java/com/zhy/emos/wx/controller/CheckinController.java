package com.zhy.emos.wx.controller;


import cn.hutool.core.date.DateUtil;
import com.zhy.emos.wx.common.util.R;
import com.zhy.emos.wx.config.shiro.JWTUtil;
import com.zhy.emos.wx.service.CheckinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/checkin")
@RestController
@Api("签到模块Web接口")
@Slf4j
public class CheckinController {
    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private CheckinService checkinService;

    @GetMapping("/validCanCheckIn")
    @ApiOperation("查看用户今天是否可以签到")
    public R validCanCheckIn(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        String result = checkinService.validCanCheckin(userId, DateUtil.now());
        return R.ok(result);
    }
}
