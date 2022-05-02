package com.zhy.emos.wx;

import cn.hutool.core.util.StrUtil;
import com.zhy.emos.wx.config.SystemConstants;
import com.zhy.emos.wx.db.dao.SysConfigDao;
import com.zhy.emos.wx.db.pojo.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@EnableAsync
@SpringBootApplication
@ServletComponentScan
@Slf4j
public class EmosWxApiApplication {
    @Autowired
    private SysConfigDao sysConfigDao;

    @Autowired
    private SystemConstants systemConstants;

    @Value("${emos.image-folder}")
    private String imageFolder;

    public static void main(String[] args) {
        SpringApplication.run(EmosWxApiApplication.class, args);
    }

    @PostConstruct
    public void init() {
        List<SysConfig> sysConfigs = sysConfigDao.selectAll();
        sysConfigs.forEach(item -> {
            String key = StrUtil.toCamelCase(item.getParamKey());
            String value = item.getParamValue();
            try {
                Field field = systemConstants.getClass().getDeclaredField(key);
                field.set(systemConstants, value);
            } catch (Exception e) {
                log.error("init error", e);
            }
        });

        new File(imageFolder).mkdirs();
    }
}
