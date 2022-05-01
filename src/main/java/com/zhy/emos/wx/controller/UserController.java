package com.zhy.emos.wx.controller;


import com.zhy.emos.wx.common.util.R;
import com.zhy.emos.wx.config.shiro.JWTUtil;
import com.zhy.emos.wx.controller.form.LoginForm;
import com.zhy.emos.wx.controller.form.RegisterForm;
import com.zhy.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/user")
@Api("用户模块Web接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public R register(@Valid @RequestBody RegisterForm form) {
        Integer userId = userService.registerUser(form.getRegisterCode(), form.getCode(), form.getNickname(), form.getPhoto());
        String token = jwtUtil.createToken(userId);
        Set<String> permissionSet = userService.searchUserPermissions(userId);
        saveCacheToken(token, userId);
        return R.ok("用户注册成功").put("token", token).put("permissions", permissionSet);
    }


    @PostMapping("/login")
    @ApiOperation("用户登录")
    public R login(@Valid @RequestBody LoginForm form) {
        Integer userId = userService.login(form.getCode());
        String token = jwtUtil.createToken(userId);
        Set<String> permissionSet = userService.searchUserPermissions(userId);
        saveCacheToken(token, userId);
        return R.ok("用户登录成功").put("token", token).put("permissions", permissionSet);
    }

    private void saveCacheToken(String token, int userId) {
        redisTemplate.opsForValue().set(token, userId + "", cacheExpire);
    }
}
