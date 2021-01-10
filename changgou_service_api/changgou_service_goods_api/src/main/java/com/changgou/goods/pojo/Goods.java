package com.changgou.goods.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品信息组合对象
 * List<Sku>
 * Spu
 *
 * spu 是指的通用属性
 * sku 是指的特有属性
 */
@Data
public class Goods implements Serializable {

    //Spu信息
    private Spu spu;

    // Sku集合
    private List<Sku> skuList;
}
