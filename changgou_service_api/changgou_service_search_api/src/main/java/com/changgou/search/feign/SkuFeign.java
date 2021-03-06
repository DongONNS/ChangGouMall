package com.changgou.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "search")
@RequestMapping(value = "/search")
public interface SkuFeign {
    /**
     * 调用搜索实现
     * @param searchMap
     * @return
     * @throws Exception
     */
    @GetMapping
    // 允许用户输入为空
    public Map search(@RequestParam(required = false) Map<String,String> searchMap) throws Exception;
}
