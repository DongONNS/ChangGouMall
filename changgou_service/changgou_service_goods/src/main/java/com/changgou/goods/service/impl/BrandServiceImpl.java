package com.changgou.goods.service.impl;

import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.service.BrandService;
import com.changgou.goods.pojo.Brand;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {

    @Resource
    private BrandMapper brandMapper;

    /**
     * 根据分类id查询品牌集合
     * @param categoryId
     * @return
     */
    @Override
    public List<Brand> findByCategory(Integer categoryId) {
        //两种方案:
        //1. 自己写sql语句直接执行  推荐
        //2. 调用通用的mapper的方法 一个个表查询
        return brandMapper.findByCategory(categoryId);
    }

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Brand> findAll() {
        return brandMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Brand findById(Integer id){
        return  brandMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     * @param brand
     */
    @Override
    public void add(Brand brand){
        brandMapper.insert(brand);
    }


    /**
     * 修改
     * @param brand
     */
    @Override
    public void update(Brand brand){
        brandMapper.updateByPrimaryKey(brand);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Integer id){
        brandMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Brand> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return brandMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Brand> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Brand>)brandMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Brand> findPage(Map<String,Object> searchMap, int page, int size){
        // 这里PageHelper的作用是让后面的查询使用上limit进行，会自行进行计算
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Brand>)brandMapper.selectByExample(example);
    }

    @Override
    public List<Map> findBrandListByCategoryName(String categoryName) {
        List<Map> brandList = brandMapper.findBrandListByCategoryName(categoryName);
        return brandList;
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 品牌名称
            if(searchMap.get("name") != null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
           	}

            // 品牌图片地址
            if(searchMap.get("image") != null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
           	}

            // 品牌的首字母
            if(searchMap.get("letter") != null && !"".equals(searchMap.get("letter"))){
                criteria.andLike("letter","%"+searchMap.get("letter")+"%");
           	}

            // 品牌id
            if(searchMap.get("id") != null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }

            // 排序
            if(searchMap.get("seq") != null ){
                criteria.andEqualTo("seq",searchMap.get("seq"));
            }
        }
        return example;
    }

}
