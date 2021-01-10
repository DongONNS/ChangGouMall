package com.changgou.order.service;

import com.changgou.order.pojo.OrderItem;

import java.util.List;

public interface CartService {
    /**
     * 加入购物车实现
     * @param num 商品数量
     * @param id  商品id
     * @param username 用户名
     */
    void add(Integer num,Long id,String username);

    /**
     * 购物车集合查询
     * @param username 用户登录名
     * @return
     */
    List<OrderItem> list(String username);
}
