package com.changgou.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;

/**
 * 令牌的解析和生成实现
 */
public class JWTTest {

    /**
     * 生成令牌
     */
    @Test
    public void testCreateToken(){
        JwtBuilder builder = Jwts.builder();

        builder.setIssuer("中南大学");       // 颁发者
        builder.setIssuedAt(new Date());   // 颁发时间
        builder.setSubject("JWT令牌测试");   // 主题信息
        builder.signWith(SignatureAlgorithm.HS256,"CSU".getBytes());

        String token = builder.compact();
        System.out.println("令牌信息为:" + token);
    }

    /**
     * 解析密钥
     */
    @Test
    public void parseToken(){
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiLkuK3ljZflpKflraYiLCJpYXQiOjE2MDE5ODU1MzUsInN1YiI6IkpXVOS7pOeJjOa1i-ivlSJ9.aZXnFb16SZNBAyhbVn3BtnkWYbh5JoZOMz4Yv88YX7g";
        Claims claim = Jwts.parser()
                .setSigningKey("CSU".getBytes())
                .parseClaimsJws(token)
                .getBody();

        System.out.println(claim);
    }
}
