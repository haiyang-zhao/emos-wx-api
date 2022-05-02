package com.zhy.emos.wx.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.zhy.emos.wx.config.SystemConstants;
import com.zhy.emos.wx.db.dao.*;
import com.zhy.emos.wx.db.pojo.TbCheckin;
import com.zhy.emos.wx.db.pojo.TbFaceModel;
import com.zhy.emos.wx.exception.EmosException;
import com.zhy.emos.wx.service.CheckinService;
import com.zhy.emos.wx.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;

@Service
@Scope("prototype")
@Slf4j
public class CheckinServiceImpl implements CheckinService {

    private static final int WORKDAY_TYPE = 1;
    private static final int HOLIDAY_TYPE = 2;

    @Autowired
    private SystemConstants constants;

    @Autowired
    private TbHolidaysDao holidaysDao;

    @Autowired
    private TbWorkdayDao workdayDao;

    @Autowired
    private TbCheckinDao checkinDao;

    @Autowired
    private TbFaceModelDao faceModelDao;

    @Autowired
    private TbCityDao cityDao;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private EmailTask emailTask;

    @Value("${emos.face.create-face-url}")
    private String createFaceUrl;

    @Value("${emos.face.checkin-url}")
    private String checkinUrl;

    @Value("${emos.email.hr}")
    private String hrEmail;

    @Value("${emos.code}")
    private String code;

    @Value("${emos.face.can-use}")
    private boolean faceCanUse;


    @Override
    public String validCanCheckin(int userId, String date) {
        boolean isHoliday = holidaysDao.searchTodayIsHolidays() != null;
        boolean isWorkday = workdayDao.searchToadyIsWorkday() != null;
        int dayType = WORKDAY_TYPE;
        if (DateUtil.date().isWeekend() || isHoliday) {
            dayType = HOLIDAY_TYPE;
        } else if (isWorkday) {
            dayType = WORKDAY_TYPE;
        }
        if (dayType == HOLIDAY_TYPE) {
            return "节假日不需要考勤";
        } else {
            DateTime now = DateUtil.date();
            String start = DateUtil.today() + " " + constants.attendanceStartTime;
            String end = DateUtil.today() + " " + constants.attendanceEndTime;
            DateTime attendanceStart = DateUtil.parse(start);
            DateTime attendanceEnd = DateUtil.parse(end);
            if (now.isBefore(attendanceStart)) {
                return "没到考勤开始时间";
            } else if (now.isAfter(attendanceEnd)) {
                return "超过考勤结束时间";
            } else {
                HashMap<String, Object> params = new HashMap<>();
                params.put("userId", userId);
                params.put("date", date);
                params.put("start", start);
                params.put("end", end);

                boolean hasCheckIn = checkinDao.haveCheckIn(params) != null;
                return hasCheckIn ? "今日已经考勤，不用重复考勤" : "可以考勤";
            }
        }
    }

    @Override
    public void checkin(HashMap<String, Object> params) {
        String address = (String) params.get("address");
        String country = (String) params.get("country");
        String province = (String) params.get("province");

        DateTime nowDateTime = DateUtil.date();
        DateTime attendanceTime = DateUtil.parse(DateUtil.today() + " " + constants.attendanceTime);
        DateTime attendanceEndTime = DateUtil.parse(DateUtil.today() + " " + constants.attendanceEndTime);
        int status = 1;

        if (nowDateTime.compareTo(attendanceTime) <= 0) {
            //正常考勤
            status = 1;
        } else if (nowDateTime.compareTo(attendanceTime) > 0 && nowDateTime.compareTo(attendanceEndTime) < 0) {
            //迟到
            status = 2;
        }
        int userId = (Integer) params.get("userId");

        if (faceCanUse) {
            String faceModel = faceModelDao.searchModelById(userId);
            if (StrUtil.isEmpty(faceModel)) {
                throw new EmosException("不存在人脸模型");
            }
            String path = (String) params.get("path");
            HttpRequest request = HttpUtil.createPost(checkinUrl);
            //上传照片
            request.form("photo", FileUtil.file(path), "targetModel", faceModel);
            HttpResponse response = request.execute();
            if (response.getStatus() != 200) {
                log.error("人脸识别服务异常");
                throw new EmosException("人脸识别服务异常");
            }
            String body = response.body();
            if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)) {
                throw new EmosException(body);
            }
            if ("False".equals(body)) {
                throw new EmosException("签到无效,非本人签到");
            }

        }
        //查询疫情风险等级
        int risk = 1;//1-低风险，2-中风险，3-高风险
        String city = (String) params.get("city");
        String district = (String) params.get("district");
        if (StrUtil.isNotEmpty(city) && StrUtil.isNotEmpty(district)) {
            String code = cityDao.searchCode(city);
            try {
                String url = "http://m." + code + ".bendibao.com/news/yqdengji/?qu=" + district;
                Document doc = Jsoup.connect(url).get();
                //通过css查找div标签
                Elements elements = doc.getElementsByClass("list-content");
                if (elements.size() > 0) {
                    Element element = elements.get(0);
                    String result = element.select("p:last-child").text();
                    if ("高风险".equals(result)) {
                        risk = 3;


                    } else if ("中风险".equals(result)) {
                        risk = 2;
                    } else {
                        risk = 1;
                        sendEmail(userId);
                    }
                }

            } catch (Exception e) {
                log.error("查询疫情分析等级失败", e);
            }
        }
        //保存签到记录
        TbCheckin entity = new TbCheckin();
        entity.setUserId(userId);
        entity.setAddress(address);
        entity.setCity(city);
        entity.setCountry(country);
        entity.setProvince(province);
        entity.setDistrict(district);
        entity.setStatus((byte) status);
        entity.setDate(LocalDate.now());
        entity.setRisk(risk);
        entity.setCreateTime(LocalDateTime.now());
        checkinDao.insert(entity);

    }

    private void sendEmail(int userId) {
        //发送邮件
        HashMap<String, String> map = userDao.searchNameAndDept(userId);
        String name = map.get("name");
        String deptName = map.get("dept_name") == null ? "" : map.get("dept_name");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(hrEmail);
        message.setSubject("员工 " + name + " 身处高风险疫情地区告警");
        message.setText("员工 " + name + ",部门 " + deptName + DateUtil.format(new Date(), "yyyy-MM-dd"));
        emailTask.sendAsync(message);
    }

    @Override
    public void createFaceModel(int userId, String path) {

        HttpRequest request = HttpUtil.createPost(createFaceUrl);
        request.form("photo", FileUtil.file(path));
        String body = request.execute().body();
        if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)) {
            throw new EmosException(body);
        }
        TbFaceModel entity = new TbFaceModel();
        entity.setUserId(userId);
        entity.setFaceModel(body);
        faceModelDao.insert(entity);

    }
}
