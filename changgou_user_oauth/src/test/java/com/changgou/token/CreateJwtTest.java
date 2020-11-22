package com.changgou.token;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.JwtHandler;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

public class CreateJwtTest {

    @Test
    public void testCreateToken(){
        // 加载证书 读取类路径中的文件
        ClassPathResource resource = new ClassPathResource("changgou.jks");

        // 读取证书数据
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, "changgou".toCharArray());

        // 获取证书中的一对密钥
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("changgou","changgou".toCharArray());

        // 获取私钥 -> RSA算法
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        // 创建令牌，需要私钥加盐[RSA算法]
        Map<String,Object> payload = new HashMap<>();
        payload.put("nikename","tomcat");
        payload.put("address","sz");
        payload.put("authorities",new String[]{"admin","user"});

        Jwt jwt = JwtHelper.encode(JSON.toJSONString(payload), new RsaSigner(privateKey));

        String token = jwt.getEncoded();
        System.out.println(token);
    }

    @Test
    public void testParseToken(){
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoic3oiLCJuaWtlbmFtZSI6InRvbWNhdCIsImF1dGhvcml0aWVzIjpbImFkbWluIiwidXNlciJdfQ.kyoKXV5tHU3HAkIXUZ84QpT_xyCdE4MqVb7OptX399kb6DOUlU5EUTi2EEDWXSaEIooJkcdUaoYAr_b2EV3GArrMEPWl2dqBeiYQi2SZFLdNw8Io66833oN_W64xf_zPUv_f9N-GD172wCIinTFFdZpvuFI0bOppcpIoJgciJwQqryZ4f1RRJHEOc1NwQ9SzGWrHR1hWE6d0sIovRemrCt5cj-Q2jwzfKE8_V6E5jrQzNLDkytgnwF5aedrEh4C1BUX14IdDxZskWq5wdPb15ECdSLzN9mli2LkUTtA7DsITTO1BeLSMbLqnoYIttvGXtHdb05hVJ3lX3e8Bi25biA";
        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm4QLPtxj6wn5r7ULYPAk547+C0JdzpaVi046mvOB6VIApL601AItfhXp6h/3ZyyVD92AgwkATBhgUAQYGD6GDLaYSsYN/RUAz1Jm1SW5zcCgaq2Of+JFhRGV3uRrsVj8Iojh6fOqWaH7XQWiKNUJ7VDmaecTFQyHQE3UvEF0AW5jaikoOUqRtpmYNoHxuZ+8xY32C3PNQdFE52BVeyb3IKimTqZ8Z+EKHBebEKvrSbpsMJm75IkpkmI+d2Pa4yJbnNpeo0MR1G6l3Yk9z7xn2g2ThURk77eaLFvMtgXciBNAOQcjvM57mh0HoZN5mE06v0P2zIzeUMCRXr4/uGt6GQIDAQAB-----END PUBLIC KEY-----";

        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publicKey));

        String claims = jwt.getClaims();
        System.out.println(claims);
    }
}
