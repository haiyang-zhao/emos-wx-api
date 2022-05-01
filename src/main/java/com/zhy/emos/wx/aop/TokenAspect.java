package com.zhy.emos.wx.aop;


import com.zhy.emos.wx.common.util.R;
import com.zhy.emos.wx.config.shiro.ThreadLocalToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class TokenAspect {

    @Autowired
    private ThreadLocalToken threadLocalToken;


    //拦截com.zhy.emos.wx.controller下所有方法
    @Pointcut("execution(public * com.zhy.emos.wx.controller.*.*(..))")
    public void aspect() {

    }

    //Around 方法执行前后和执行后都可以拦截
    @Around("aspect()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        //方法执行结果
        R r = (R) point.proceed();
        String token = threadLocalToken.getToken();
        //如果ThreadLocal中存在Token，说明更新的Token
        if (token != null) {
            //向响应中放置Token
            r.put("token", token);
            threadLocalToken.clear();
        }
        return r;
    }

}