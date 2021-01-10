package com.changgou.filter;

import com.changgou.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    //令牌头名字
    private static final String AUTHORIZE_TOKEN = "Authorization";

    /***
     * 全局过滤器
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取request response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 获取用户令牌信息
        // 1）头文件中
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        // boolean true:令牌在头文件中 false:令牌不在头文件中-->将令牌封装到头文件中，再传递给其他微服务
        // 将令牌信息放入到头中是为了进行后期OAuth2.0的校验使用
        boolean hasToken = true;

        // 2）参数获取令牌(不存在于头文件中)
        if (StringUtils.isEmpty(token)){
            token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
            hasToken = false;
        }
        // 3）cookie获取令牌(不存在于头文件和参数中)
        if (StringUtils.isEmpty(token)){
            HttpCookie httpCookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if (httpCookie != null){
                token = httpCookie.getValue();
            }
        }
        // 判断令牌是否为空 如果不为空 则将令牌放到头文件中 放行

        // 如果没有令牌 则拦截
        if (StringUtils.isEmpty(token)){
            // 设置没有权限的状态码 401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // 响应空数据
            return response.setComplete();
        } else{
            // 如果请求头中还没有令牌，将其封装到请求头中
            if (!hasToken){
                // 判断当前l令牌是否有bearer前缀，如果没有，则添加bearer前缀
                if (!token.startsWith("bearer ") && !token.startsWith("Bearer ")){
                    token = "bearer " + token;
                }

                request.mutate().header(AUTHORIZE_TOKEN,token);
            }
        }

        // 有效则放行
        return chain.filter(exchange);

//        // 如果有令牌，则校验令牌是否有效
//        try {
//            // 因为之前没有用到证书，但是这里需要使用证书，所以是不能这样用了
//            // JwtUtil.parseJWT(token);
//        } catch (Exception e) {
//            // 无效则拦截(解析异常说明令牌是无效的)
//            // 设置没有权限的状态码 401
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            // 响应空数据
//            return response.setComplete();
//        }

        // 放行之前，将token放到头中
//        request.mutate().header(AUTHORIZE_TOKEN,token);


    }

    @Override
    public int getOrder() {
        return 0;
    }
}
