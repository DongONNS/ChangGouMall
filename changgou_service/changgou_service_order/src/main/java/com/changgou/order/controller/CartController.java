package com.changgou.order.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.entity.TokenDecode;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 购物车操作
 */
@RestController
@RequestMapping(value = "/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 加入购物车
     * 1.加入购物车的数量
     * 2.商品的id
     */
    @GetMapping(value = "/add")
    public Result add(Integer num, Long id){
        Map<String, String> userInfo = TokenDecode.getUserInfo();

        String username = userInfo.get("username");

        cartService.add(num,id,username);
        return new Result(true, StatusCode.OK,"加入购物车成功");
    }

    /**
     * 购物车列表
     */
    @GetMapping(value = "/list")
    public Result<List<OrderItem>> list(){
        // 获取用户登录名
        // OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
        // String token = details.getTokenValue();

        Map<String, String> userInfo = TokenDecode.getUserInfo();

        String username = userInfo.get("username");
//        String username = "szitheima";    // 这里是直接写死，不提倡直接写死的做法

        // 查询购物车列表
        List<OrderItem> orderItems = cartService.list(username);
        return new Result<List<OrderItem>>(true,StatusCode.OK,"查询购物车列表成功",orderItems);
    }
}
