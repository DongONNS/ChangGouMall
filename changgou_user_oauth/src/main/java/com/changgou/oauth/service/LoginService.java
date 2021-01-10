package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

/**
 * 描述
 *
 * @author www.itheima.com
 * @version 1.0
 * @package com.changgou.oauth.service *
 * @since 1.0
 */
public interface LoginService {
    /**
     * 模拟用户的行为 发送请求 申请令牌 返回
     * @param username 用户名
     * @param password 用户密码
     * @param clientId 客户端id
     * @param clientSecret 客户端密钥
     * @param grandType 授权类型
     * @return
     *     *参数传递
     *      * 1.帐号         username=szitheima
     *      * 2.密码         password=szitheima
     *      * 3.授权方式      grant_type=password
     *      * 请求头传递
     *      * 4.Basic Base64(客户端id:客户端密钥) Authorization=Basic Y2hhbmdnb3U6Y2hhbmdnb3U
     *      *
     */
    AuthToken login(String username, String password, String clientId, String clientSecret, String grandType);
}
