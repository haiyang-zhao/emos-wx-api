package com.zhy.emos.wx.db.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;

@Mapper
public interface TbCheckinDao {
    Integer haveCheckIn(HashMap<String, Object> param);
}