package com.zhy.emos.wx.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.zhy.emos.wx.config.SystemConstants;
import com.zhy.emos.wx.db.dao.TbCheckinDao;
import com.zhy.emos.wx.db.dao.TbHolidaysDao;
import com.zhy.emos.wx.db.dao.TbWorkdayDao;
import com.zhy.emos.wx.service.CheckinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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
}
