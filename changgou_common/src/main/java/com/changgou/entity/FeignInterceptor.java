package com.changgou.entity;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

public class FeignInterceptor implements RequestInterceptor {

    /**
     * Feign执行之前进行拦截
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        /**
         * 获取用户的令牌
         * 将令牌再封装到头文件中
         */

        // 记录了当前用户请求的所有数据，包含请求头和请求参数等
        // 用户当前请求的时候对应线程的数据（如果在配置中开启了熔断的话，会用新的线程，所以没法得到对应线程的数据）
        // 所以需要在配置文件中将熔断策略修改成信号量隔离，此时不会开启新的线程
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        /**
         * 获取请求头中的数据
         */
        Enumeration<String> headerNames = requestAttributes.getRequest().getHeaderNames();

        while(headerNames.hasMoreElements()){
            // 请求头的key
            String headerKey = headerNames.nextElement();

            // 获取请求头的值
            String headerValue = requestAttributes.getRequest().getHeader(headerKey);

            System.out.println(headerKey + ":" + headerValue);

            // 将请求头信息封装到头中，使用Feign调用的时候会传递给下一个微服务
            requestTemplate.header(headerKey,headerValue);
        }
    }
}
