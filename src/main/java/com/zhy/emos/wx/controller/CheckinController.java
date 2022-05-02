package com.zhy.emos.wx.controller;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.zhy.emos.wx.common.util.R;
import com.zhy.emos.wx.config.shiro.JWTUtil;
import com.zhy.emos.wx.controller.form.CheckinForm;
import com.zhy.emos.wx.exception.EmosException;
import com.zhy.emos.wx.service.CheckinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

@RequestMapping("/checkin")
@RestController
@Api("签到模块Web接口")
@Slf4j
public class CheckinController {
    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private CheckinService checkinService;

    @Value("${emos.image-folder}")
    private String imageFolder;

    @Value("${emos.face.can-use}")
    private boolean faceCanUse;

    @PostMapping("/checkin")
    @ApiOperation("用户签到")
    public R checkin(@Valid CheckinForm form,
                     @RequestParam("photo") MultipartFile file,
                     @RequestHeader("token") String token) {
        if (file == null) {
            return R.error("没有上传签到照片");
        }
        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        if (!fileName.endsWith(".jpg")) {
            return R.error("必须提交JPG格式图片");
        }
        String path = imageFolder + File.separator + fileName;
        try {
            //文件拷贝
            file.transferTo(Paths.get(path));
            HashMap<String, Object> param = new HashMap<>();
            param.put("userId", userId);
            param.put("path", path);
            param.put("city", form.getCity());
            param.put("address", form.getAddress());
            param.put("country", form.getCountry());
            param.put("province", form.getProvince());
            param.put("district", form.getDistrict());
            checkinService.checkin(param);
            return R.ok("签到成功");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new EmosException("图片保存错误");
        } finally {
            FileUtil.del(path);
        }
    }

    @GetMapping("/validCanCheckIn")
    @ApiOperation("查看用户今天是否可以签到")
    public R validCanCheckIn(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        String result = checkinService.validCanCheckin(userId, DateUtil.now());
        return R.ok(result);
    }


    @PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestParam("photo") MultipartFile file, @RequestHeader("token") String token) {
        if (!faceCanUse) {
            return R.ok("人脸模型创建成功");
        }
        if (file == null) {
            return R.error("没有上传签到照片");
        }
        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        if (!fileName.endsWith(".jpg")) {
            return R.error("必须提交JPG格式图片");
        }
        String path = imageFolder + File.separator + fileName;
        try {
            //文件拷贝
            file.transferTo(Paths.get(path));
            checkinService.createFaceModel(userId, path);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new EmosException("保存图片识别");
        } finally {
            FileUtil.del(path);
        }
        return R.ok("人脸模型创建成功");
    }

}
