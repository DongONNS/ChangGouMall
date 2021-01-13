package com.changgou.search.controller;

import com.changgou.entity.Page;
import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping(value = "/search")
public class SkuController {

    @Autowired
    private SkuFeign skuFeign;

    /**
     * 实现搜索调用
     * @return
     */
    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map<String,String> searchMap, Model model) throws Exception {

        Map<String,Object> resultMap = skuFeign.search(searchMap);
        model.addAttribute("result",resultMap);
        /**
         * 计算分页
         * total : 总记录数
         * pageNumber : 当前页
         * pageSize : 每页显示数量
         */
        long total = Long.parseLong(resultMap.get("total").toString());
        int pageNumber = Integer.parseInt(resultMap.get("pageNumber").toString());
        int pageSize = Integer.parseInt(resultMap.get("pageSize").toString());
        Page<SkuInfo> pageInfo = new Page<>(total,pageNumber,pageSize);
        model.addAttribute("pageInfo",pageInfo);

        // 将条件存储,用于页面回显数据
        model.addAttribute("searchMap",searchMap);

        // 获取上次请求地址
        String url = getUrl(searchMap);
        model.addAttribute("url",url);
        return "search";
    }

    /**
     * 拼接组装用户请求的url地址
     * 获取用户每次请求的地址
     * 页面需要在这次请求的地址上添加额外的搜索条件
     * http://localhost:18086/search/list
     * http://localhost:18086/search/list?keywords=华为
     * http://localhost:18086/search/list?keywords=华为&&brand=华为
     * http://localhost:18086/search/list?keywords=华为&&brand=华为&category=语言文字
     * @param searchMap
     * @return
     */
    public String getUrl(Map<String, String> searchMap){
        String url = "/search/list";    // 初始地址
//        String sortUrl = "/search/list";
        if (searchMap != null && searchMap.size() > 0){
            url += "?";
//            sortUrl += "?";
            for (Map.Entry<String, String> entry : searchMap.entrySet()){
                // key是搜索的条件对象
                String key = entry.getKey();
                // value是搜索的值
                String value = entry.getValue();

                // 跳过分页参数(在进行其他信息的筛选的时候应该重新从第一页开始)
                if (key.equalsIgnoreCase("pageNum")){
                    continue;
                }

                // 如果是排序,则直接进行跳过,因为我们可以找到排序的数据
                if (key.equalsIgnoreCase("sortField") || key.equalsIgnoreCase("sortRule")){
                    continue;
                }

                url = url +  key + "=" + value + "&";

//                sortUrl = sortUrl + key + "=" + value + "&";
            }
            // 去掉最后一个&符号
            url = url.substring(0,url.length() - 1);
//            sortUrl = sortUrl.substring(0,sortUrl.length() - 1);
        }
        return url;
//        return new String[]{url,sortUrl};
    }
}
