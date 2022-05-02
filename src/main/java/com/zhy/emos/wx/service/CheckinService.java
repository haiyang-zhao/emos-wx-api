package com.zhy.emos.wx.service;

import java.util.HashMap;

public interface CheckinService {
    String validCanCheckin(int userId, String date);

    void checkin(HashMap<String, Object> params);

    void createFaceModel(int userId, String path);
}
