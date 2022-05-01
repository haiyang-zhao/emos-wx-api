package com.zhy.emos.wx.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhy.emos.wx.db.dao.TbUserDao;
import com.zhy.emos.wx.exception.EmosException;
import com.zhy.emos.wx.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;


@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService {

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    @Autowired
    private TbUserDao userDao;

    private String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap<String, Object> param = new HashMap<>();
        param.put("appid", appId);
        param.put("secret", appSecret);
        param.put("js_code", code);
        param.put("grant_type", "authorization_code");
        log.debug("param :" + param);
        String response = HttpUtil.post(url, param);
        log.debug("response: " + response);
        JSONObject jsonObject = JSONUtil.parseObj(response);
        String openId = jsonObject.getStr("openid");
        if (StrUtil.isEmpty(openId)) {
            throw new RuntimeException("获取临时凭证失败");
        }
        return openId;

    }

    @Override
    public Integer registerUser(String registerCode, String code, String nickname, String photo) {
        //如果邀请码是000000，表示创建超级管理员
        if ("000000".equals(registerCode)) {
            //查询超级管理员账号是否已经绑定
            boolean existRootUser = userDao.haveRootUser();
            if (existRootUser) {
                throw new EmosException("超级管理员已经存在");
            }
            //把当前用户绑定到Root账号
            String openId = getOpenId(code);
            HashMap<String, Object> param = new HashMap<>();
            param.put("openId", openId);
            param.put("nickname", nickname);
            param.put("photo", photo);
            param.put("role", "[0]");
            param.put("status", 1);
            param.put("create_time", new Date());
            param.put("root", true);
            userDao.insert(param);
            return userDao.searchIdByOpenId(openId);
        } else {
            //TODO 普通用户注册
        }
        return null;
    }

    @Override
    public Set<String> searchUserPermissions(int userId) {
        return userDao.searchUserPermissions(userId);
    }

    @Override
    public Integer login(String code) {
        String openId = getOpenId(code);
        Integer userId = userDao.searchIdByOpenId(openId);
        if (userId == null) {
            throw new EmosException("该用户不存在");
        }
        //TODO 从消息队列中接受消息，转移到消息表
        return userId;
    }
}
