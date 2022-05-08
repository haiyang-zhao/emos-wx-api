package com.zhy.emos.wx.service;

import java.util.HashMap;
import java.util.List;

public interface CheckinService {
    String validCanCheckin(int userId, String date);

    void checkin(HashMap<String, Object> params);

    void createFaceModel(int userId, String path);

    HashMap<String, Object> searchTodayCheckin(int userId);

    long searchCheckinDays(int userId);

    List<HashMap<String, Object>> searchWeekCheckin(HashMap<String, Object> param);
}
