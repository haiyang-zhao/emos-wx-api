package com.zhy.emos.wx.config.shiro;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype") //表示多例
@Slf4j
public class OAuth2Filter extends AuthenticatingFilter {

    @Autowired
    private ThreadLocalToken threadLocalToken;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        log.debug("OAuth2Filter---->");
        super.doFilterInternal(request, response, chain);
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        String token = getRequestToken(req);
        if (StrUtil.isEmpty(token)) {
            return null;
        }
        return new OAuth2Token(token);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest req = (HttpServletRequest) request;
        //options请求不拦截
        return req.getMethod().equals(RequestMethod.OPTIONS.name());
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        //处理跨域问题
        // 接受任意域名的请求
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        // 允许浏览器发送Cookie
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        threadLocalToken.clear();
        String token = getRequestToken(req);
        if (StrUtil.isEmpty(token)) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.getWriter().println("认证失败");
            return false;
        }
        try {
            jwtUtil.verifyToken(token);
        } catch (TokenExpiredException e) { //Token过期
            //服务器端没有过期，redis中过期时间是Token过期时间的2倍
            if (redisTemplate.hasKey(token)) {
                redisTemplate.delete(token);
                int userId = jwtUtil.getUserId(token);
                String newToken = jwtUtil.createToken(userId);
                redisTemplate.opsForValue().set(token, userId + "", cacheExpire, TimeUnit.DAYS);
                threadLocalToken.setToken(newToken);

            } else {
                //真过期
                resp.setStatus(HttpStatus.UNAUTHORIZED.value());
                resp.getWriter().println("认证过期");
                return false;
            }
        } catch (Exception e) {
            //Token无效
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.getWriter().println("无效Token");
            return false;
        }
        //间接地让shiro执行Realm类
        return executeLogin(request, response);
    }


    @Override
    protected boolean onLoginFailure(AuthenticationToken token,
                                     AuthenticationException e,
                                     ServletRequest request,
                                     ServletResponse response) {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        //处理跨域问题
        // 接受任意域名的请求
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        // 允许浏览器发送Cookie
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        try {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.getWriter().println(e.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private String getRequestToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }
        return token;
    }
}
