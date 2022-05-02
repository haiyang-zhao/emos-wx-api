package com.zhy.emos.wx.service;

import com.zhy.emos.wx.db.pojo.TbUser;

import java.util.Set;

public interface UserService {
    Integer registerUser(String registerCode, String code, String nickname, String photo);

    Set<String> searchUserPermissions(int userId);

    Integer login(String code);

    TbUser searchById(int userId);
}
