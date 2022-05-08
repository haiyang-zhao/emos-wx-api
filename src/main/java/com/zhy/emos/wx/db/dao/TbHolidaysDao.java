package com.zhy.emos.wx.db.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface TbHolidaysDao {
    Integer searchTodayIsHolidays();

    List<String> searchHolidaysInRange(HashMap param);
}