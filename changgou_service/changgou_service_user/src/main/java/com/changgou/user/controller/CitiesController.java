package com.changgou.user.controller;
import com.changgou.entity.PageResult;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.user.service.CitiesService;
import com.changgou.user.pojo.Cities;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@CrossOrigin
@RequestMapping("/cities")
public class CitiesController {

    @Autowired
    private CitiesService citiesService;

    /**
     * 查询全部数据
     * @return
     */
    @GetMapping
    public Result findAll(){
        List<Cities> citiesList = citiesService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",citiesList) ;
    }

    /***
     * 根据ID查询数据
     * @param cityId
     * @return
     */
    @GetMapping("/{cityId}")
    public Result findById(@PathVariable String cityId){
        Cities cities = citiesService.findById(cityId);
        return new Result(true,StatusCode.OK,"查询成功",cities);
    }

    /***
     * 新增数据
     * @param cities
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Cities cities){
        citiesService.add(cities);
        return new Result(true,StatusCode.OK,"添加成功");
    }

    /***
     * 修改数据
     * @param cities
     * @param cityId
     * @return
     */
    @PutMapping(value="/{cityId}")
    public Result update(@RequestBody Cities cities,@PathVariable String cityId){
        cities.setCityId(cityId);
        citiesService.update(cities);
        return new Result(true,StatusCode.OK,"修改成功");
    }

    /***
     * 根据ID删除品牌数据
     * @param cityId
     * @return
     */
    @DeleteMapping(value = "/{cityId}" )
    public Result delete(@PathVariable String cityId){
        citiesService.delete(cityId);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap){
        List<Cities> list = citiesService.findList(searchMap);
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
        Page<Cities> pageList = citiesService.findPage(searchMap, page, size);
        PageResult pageResult=new PageResult(pageList.getTotal(),pageList.getResult());
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }
}
