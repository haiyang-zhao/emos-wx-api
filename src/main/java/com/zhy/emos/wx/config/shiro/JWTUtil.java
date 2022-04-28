package com.zhy.emos.wx.config.shiro;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class JWTUtil {

    private static final String KEY_USER_ID = "userId";
    @Value("${emos.jwt.secret}")
    private String secret;

    @Value("${emos.jwt.expire}")
    private int expire;

    public String createToken(int userId) {
        //偏移5天
        DateTime date = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, 5);
        //算法对象
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withClaim(KEY_USER_ID, userId)
                .withExpiresAt(date)
                .sign(algorithm);
    }

    public int getUserId(String token) {
        return JWT.decode(token).getClaim(KEY_USER_ID).asInt();
    }

    public void verifyToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        verifier.verify(token);
    }
}
