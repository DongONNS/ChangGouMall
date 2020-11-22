package com.changgou.oauth.config;
import com.changgou.entity.Result;
import com.changgou.oauth.util.UserJwt;
import com.changgou.user.feign.UserFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/*****
 * 自定义授权认证类
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ClientDetailsService clientDetailsService;

    @Autowired
    private UserFeign userFeign;

    /****
     * 自定义授权认证
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //============客户端信息认证 start===============

        //取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if(authentication==null){
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);

            if(clientDetails!=null){
                //秘钥
                String clientSecret = clientDetails.getClientSecret();
                //静态方式
//                return new User(username,       // 客户端id
//                                new BCryptPasswordEncoder().encode(clientSecret),   // 客户端密钥-->加密操作
//                                AuthorityUtils.commaSeparatedStringToAuthorityList(""));    // 权限

                //数据库查找方式
                return new User(username,
                                clientSecret,
                                AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }

        //============客户端信息认证 end===============

        //============用户账号密码信息认证 start===============

        if (StringUtils.isEmpty(username)) {
            return null;
        }



        /**
         * 客户端id : changgou
         * 客户端密钥 ： changgou
         * 普通账号 -> 账号 ： 任意账号 秘密：szitheima
         */
        // 根据用户名查询用户信息
        // String pwd = new BCryptPasswordEncoder().encode("szitheima");



        Result<com.changgou.user.pojo.User> userResult = userFeign.findByUsername(username);

        // 从数据库获取用户密码
        if (userResult == null || userResult.getData() == null){
            return null;
        }
        String password = userResult.getData().getPassword();

        // 获取用户权限（数据库中并没有设计权限相关的内容，所以这里就没有从数据库进行数据获取）
        String permissions = "user,vip";    // 指定用户的角色信息,这里是写死了，但是一般是通过查询数据库得到这个数据

        UserJwt userDetails = new UserJwt(username,password,AuthorityUtils.commaSeparatedStringToAuthorityList(permissions));

        //============用户帐号密码信息认证 end===============

        //userDetails.setComy(songsi);
        return userDetails;
    }
}
