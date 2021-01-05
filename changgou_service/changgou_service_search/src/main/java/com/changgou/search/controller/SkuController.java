package com.changgou.search.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.search.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /**
     * 调用搜索实现
     * @param searchMap
     * @return
     * @throws Exception
     */
    @GetMapping
    // 允许用户输入为空
    public Map search(@RequestParam(required = false) Map<String,String> searchMap) throws Exception {
        return skuService.search(searchMap);
    }

    /**
     * 导入数据
     * @return
     */
    @GetMapping(value = "/import")
    public Result importData(){
        skuService.importData();
        System.out.println("============导入数据成功===========");
        return new Result(true, StatusCode.OK,"导入数据到索引库成功");
    }
}