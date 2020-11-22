package com.changgou.goods.pojo;

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
public class Goods implements Serializable {

    //Spu信息
    private Spu spu;

    // Sku信息
    private List<Sku> skuList;

    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<Sku> skuList) {
        this.skuList = skuList;
    }
}
