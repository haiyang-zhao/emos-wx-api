package com.zhy.emos.wx.db.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbCityDao {
    String searchCode(String city);
}