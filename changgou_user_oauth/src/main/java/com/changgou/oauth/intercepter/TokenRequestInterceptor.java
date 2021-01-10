package com.changgou.oauth.intercepter;

import com.changgou.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenRequestInterceptor implements RequestInterceptor {

    /**
     * Feign执行之前进行拦截
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        /**
         * 从数据库加载查询用户信息
         * 1.没有令牌,生成令牌
         * 2.令牌需要携带过去
         * 3.令牌需要存放到头文件中
         * 4.请求 -> feign调用之前 -> 拦截器 RequestIntercepter -> feign调用之前执行拦截
         */

        // 生成admin令牌
        String token = AdminToken.adminToken();
        requestTemplate.header("Authorization","bearer " +  token);
    }
}
