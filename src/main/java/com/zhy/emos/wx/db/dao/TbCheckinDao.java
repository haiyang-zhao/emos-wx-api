package com.zhy.emos.wx.db.dao;

import com.zhy.emos.wx.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface TbCheckinDao {
    Integer haveCheckIn(HashMap<String, Object> param);

    void insert(TbCheckin checkin);

    HashMap<String, Object> searchTodayCheckin(int userId);

    Long searchCheckinDays(int userId);

    //查询员工本周签到情况
    List<HashMap<String, Object>> searchWeekCheckin(HashMap<String, Object> param);
}