package com.zhy.emos.wx.db.dao;

import com.zhy.emos.wx.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;

@Mapper
public interface TbCheckinDao {
    Integer haveCheckIn(HashMap<String, Object> param);

    void insert(TbCheckin checkin);
}