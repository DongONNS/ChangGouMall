package com.changgou.order.service.impl;

import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import com.changgou.order.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    // 数据存入到哪台机器的redis中
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;

    /**
     * 购物车集合查询
     * @param username 用户登录名
     * @return
     */
    @Override
    public List<OrderItem> list(String username) {
        // 获取指定命名空间下的所有数据
        return redisTemplate.boundHashOps("Cart_" + username).values();
    }


    /**
     * 加入购物车
     * @param num 商品数量
     * @param id  商品id
     * @param username 用户名
     */
    @Override
    public void add(Integer num, Long id ,String username) {

        // 当添加购物车数量 <= 0时候，需要移除该商品信息
        if (num <= 0){
            // 移除购物车该商品
            redisTemplate.boundHashOps("Cart_" + username).delete(id);

            //如果此时购物车数量为空，则连购物车一起移除
            Long size = redisTemplate.boundHashOps("Cart_" + username).size();
            if (size == null || size <= 0){
                redisTemplate.delete("Cart_" + username);
            }
            return;
        }


        // 查询商品详情
        // 1) 查询Sku
        Result<Sku> skuResult = skuFeign.findById(id + "");

        Sku sku = skuResult.getData();

        // 2) 查询Spu

        Result<Spu> spuResult = spuFeign.findById(sku.getSpuId());
        Spu spu = spuResult.getData();

        // 将加入购物车的商品信息封装成OrderItem
        OrderItem orderItem = createOrderItem(num, id, sku, spu);

        // 将购物车数据存入到redis中 ： namespace --> username
        redisTemplate.boundHashOps("Cart_" + username).put(id,orderItem);
    }



    // 将加入购物车的商品信息封装成OrderItem
    private OrderItem createOrderItem(Integer num, Long id, Sku sku, Spu spu) {

        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId().toString());
        orderItem.setSkuId(id);
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num * orderItem.getPrice());
        orderItem.setImage(spu.getImage());
        return orderItem;
    }
}
