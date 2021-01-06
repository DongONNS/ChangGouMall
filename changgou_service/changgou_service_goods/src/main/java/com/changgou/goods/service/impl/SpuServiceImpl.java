package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.IdWorker;
import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.dao.SkuMapper;
import com.changgou.goods.dao.SpuMapper;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService {

    @Resource
    private SpuMapper spuMapper;

    @Resource
    private SkuMapper skuMapper;

    // 用于查询三级分类名称
    @Resource
    private CategoryMapper categoryMapper;

    // 用于查询品牌信息
    @Resource
    private BrandMapper brandMapper;

    /**
     * 用于生成sku id 和 spu id
     */
    @Autowired
    private IdWorker idWorker;

    /**
     * 批量上架
     * @param ids 要上架的所有商品id(spuId)
     */
    @Override
    public void putMany(Long[] ids) {
        // update tb_sku set IsMarketable = 1 where id in (ids) and isDelete = 0 and status = 1
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        // id in (ids)
        criteria.andIn("id", Arrays.asList(ids));;
        // 未删除
        criteria.andEqualTo("isDelete","0");
        // 已审核
        criteria.andEqualTo("status","1");

        // 准备修改的数据
        Spu spu = new Spu();
        spu.setIsMarketable("1");  // 上架
        spuMapper.updateByExampleSelective(spu,example);
    }

    @Override
    public void put(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        // 检查是否删除的商品
        if (spu.getIsDelete().equalsIgnoreCase("1")){
            throw new RuntimeException("此商品已删除");
        }

        if (spu.getStatus().equalsIgnoreCase("1")){
            throw new RuntimeException("该商品未通过审核！");
        }

        // 上架状态
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 商品下架
     * @param spuId
     */
    @Override
    public void pull(Long spuId) {
        // 查询商品
        Spu spu = spuMapper.selectByPrimaryKey(spuId);

        // 判断商品是否符合下架条件
        if (spu.getIsDelete().equalsIgnoreCase("1")){
            throw new RuntimeException("不能对已经删除商品进行下架");
        }
        // 修改状态
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 实现商品的审核
     * @param spuId
     */
    @Override
    public void audit(Long spuId) {
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(spuId);

        // 判断商品是否符合审核条件
        if (spu.getIsDelete().equalsIgnoreCase("1")){
            throw new RuntimeException("不能对已删除商品进行审核！");
        }

        // 审核 修改状态
        spu.setStatus("1");
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 根据ID查询goods数据
     * @param id spu的id
     * @return
     */
    @Override
    public Goods findGoodsById(Long id) {
        // 查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);

        // 查询List<sku> -->spuId select * from tb_sku where spu_id = ?
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skuList = skuMapper.select(sku);

        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skuList);

        return goods;
    }

    /**
     * 添加商品信息
     * @param goods
     */
    @Override
    public void saveGoods(Goods goods) {
        // spu --> 一个
        Spu spu = goods.getSpu();
        // 判断spu的id是否为空
        if(spu.getId() != null){
            // 不为空，则为增加数据
            spu.setId(idWorker.nextId());
            spuMapper.insertSelective(spu);
        } else{
            // 否则为修改数据，修改spu
            spuMapper.updateByPrimaryKeySelective(spu);

            // 删除之前的List<Sku> delete from tb_sku where spu_id = ?
            Sku sku = new Sku();
            sku.setSpuId(spu.getId());
            skuMapper.delete(sku);
        }

        // 获取sku所在的三级分类
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());

        // 品牌信息
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());

        // sku --> List集合
        Date currentDate = new Date(); // 创建时间和更新时间

        List<Sku> skuList = goods.getSkuList();
        for (Sku sku : skuList){
            sku.setId(idWorker.nextId());

            // Spu.name + 规格信息
            String name = spu.getName();

            // 防止spec为空的情况
            if (StringUtils.isEmpty(sku.getSpec())){
                sku.setSpec("{}");
            }

            // 将spec转成map
            Map<String,String> specMap = JSON.parseObject(sku.getSpec(), Map.class);

            for (Map.Entry<String,String> entry : specMap.entrySet()){
                name += " " + entry.getValue();
            }

            sku.setName(name);
            sku.setCreateTime(currentDate);
            sku.setUpdateTime(currentDate);

            // 三级分类id可以在spu中找到
            sku.setCategoryId(spu.getCategory3Id());

            // 使用categoryMapper 用三级分类id查找三级分类名称
            sku.setCategoryName(category.getName());  // --> 三级分类名字
            sku.setBrandName(brand.getName());     // --> 品牌名称

            // 将sku添加到数据库中
            skuMapper.insertSelective(sku);
        }
    }

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Spu findById(String id){
        return  spuMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     * @param spu
     */
    @Override
    public void add(Spu spu){
        spuMapper.insert(spu);
    }


    /**
     * 修改
     * @param spu
     */
    @Override
    public void update(Spu spu){
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        spuMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Spu> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Spu>)spuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Spu> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Spu>)spuMapper.selectByExample(example);
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andEqualTo("id",searchMap.get("id"));
           	}
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andEqualTo("sn",searchMap.get("sn"));
           	}
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
           	}
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
           	}
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
           	}
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
           	}
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
           	}
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
           	}
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
           	}
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
           	}
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andEqualTo("isMarketable",searchMap.get("isMarketable"));
           	}
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andEqualTo("isEnableSpec", searchMap.get("isEnableSpec"));
           	}
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andEqualTo("isDelete",searchMap.get("isDelete"));
           	}
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andEqualTo("status",searchMap.get("status"));
           	}

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
