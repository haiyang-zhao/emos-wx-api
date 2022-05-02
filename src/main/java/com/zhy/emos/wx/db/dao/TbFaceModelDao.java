package com.zhy.emos.wx.db.dao;

import com.zhy.emos.wx.db.pojo.TbFaceModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbFaceModelDao {
    String searchModelById(int userId);

    void insert(TbFaceModel faceModel);

    void deleteFaceModel(int userId);

}