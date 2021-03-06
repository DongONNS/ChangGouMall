package com.changgou.goods.service;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.Page;

import java.util.List;
import java.util.Map;

public interface SpuService {

    /**
     * 批量上架
     * @param ids 要上架的所有商品id(spuId)
     */
    void putMany(Long[] ids);

    /**
     * 商品上架
     * @param spuId
     */
    void put(Long spuId);

    /**
     * 商品下架
     * @param spuId
     */
    void pull(Long spuId);

    /**
     * 商品的审核
     * @param spuId
     */
    void audit(Long spuId);

    /**
     * 逻辑删除
     * @param spuId
     */
    void logicDelete(Long spuId);

    /**
     * 还原被删除商品
     * @param spuId
     */
    void restore(Long spuId);

    /**
     * 根据id查询goods信息
     * @parameter id spuId
     */
    Goods findGoodsById(Long id);

    /**
     * 商品增加
     */
    void saveGoods(Goods goods);

    /***
     * 查询所有
     * @return
     */
    List<Spu> findAll();

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    Spu findById(String id);

    /***
     * 新增
     * @param spu
     */
    void add(Spu spu);

    /***
     * 修改
     * @param spu
     */
    void update(Spu spu);

    /***
     * 删除
     * @param id
     */
    void delete(String id);

    /***
     * 多条件搜索
     * @param searchMap
     * @return
     */
    List<Spu> findList(Map<String, Object> searchMap);

    /***
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    Page<Spu> findPage(int page, int size);

    /***
     * 多条件分页查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    Page<Spu> findPage(Map<String, Object> searchMap, int page, int size);
}
