package com.changgou.goods.controller;
import com.changgou.entity.PageResult;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.pojo.Goods;
import com.changgou.goods.service.SpuService;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@CrossOrigin
@RequestMapping("/spu")
public class SpuController {


    @Autowired
    private SpuService spuService;

    /**
     * 批量上架
     * @param ids
     * @return
     */
    @PutMapping("/put/many")
    public Result putMany(@RequestBody Long[] ids){
        spuService.putMany(ids);
        int count = ids.length;
        return new Result(true,StatusCode.OK,"上架成功！");
    }

    /**
     * 上架操作
     * @param spuId
     * @return
     */
    @PutMapping(value = "put/{id}")
    public Result put(@PathVariable(value = "id") Long spuId){
        spuService.put(spuId);
        return new Result(true,StatusCode.OK,"上架成功！");
    }

    /**
     * 下架操作
     * @param spuId
     * @return
     */
    @PutMapping(value = "pull/{id}")
    public Result pull(@PathVariable(value = "id") Long spuId){
        spuService.pull(spuId);
        return new Result(true,StatusCode.OK,"下架成功！");
    }

    /**
     * 审核操作
     * @param spuId
     * @return
     */
    @PutMapping(value = "audit/{id}")
    public Result audit(@PathVariable(value = "id") Long spuId){
        spuService.audit(spuId);
        return new Result(true,StatusCode.OK,"审核通过！");
    }

    /**
     * 根据SpuId查询Goods信息
     */
    @GetMapping(value = "/goods/{id}")
    public Result<Goods> findGoodsById(@PathVariable(value = "id")Long spuId){
        Goods goods = spuService.findGoodsById(spuId);
        return new Result<Goods>(true, StatusCode.OK,"查询Goods成功",goods);
    }

    /**
     * 增加商品的实现
     */
    @PostMapping("/save")
    public Result saveGoods(@RequestBody Goods goods){
        spuService.saveGoods(goods);
        return new Result(true,StatusCode.OK,"商品添加成功");
    }

    /**
     * 查询全部数据
     * @return
     */
    @GetMapping
    public Result findAll(){
        List<Spu> spuList = spuService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",spuList) ;
    }

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable String id){
        Spu spu = spuService.findById(id);
        return new Result(true,StatusCode.OK,"查询成功",spu);
    }


    /***
     * 新增数据
     * @param spu
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Spu spu){
        spuService.add(spu);
        return new Result(true,StatusCode.OK,"添加成功");
    }


    /***
     * 修改数据
     * @param spu
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody Spu spu,@PathVariable Long id){
        spu.setId(id);
        spuService.update(spu);
        return new Result(true,StatusCode.OK,"修改成功");
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable String id){
        spuService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap){
        List<Spu> list = spuService.findList(searchMap);
        return new Result(true,StatusCode.OK,"查询成功",list);
    }


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result findPage(@RequestParam Map searchMap, @PathVariable  int page, @PathVariable  int size){
        Page<Spu> pageList = spuService.findPage(searchMap, page, size);
        PageResult pageResult=new PageResult(pageList.getTotal(),pageList.getResult());
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }


}
