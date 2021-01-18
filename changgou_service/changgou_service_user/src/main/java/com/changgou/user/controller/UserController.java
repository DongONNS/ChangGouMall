package com.changgou.user.controller;
import com.alibaba.fastjson.JSON;
import com.changgou.entity.*;
import com.changgou.user.service.UserService;
import com.changgou.user.pojo.User;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/login")
    public Result login(String username, String password, HttpServletResponse response){
        // 查询用户信息
        User user = userService.findById(username);

        // 对比密码-->密码对比:加密
        // 下面这种方式是不正确的，我们的user.getPassword得到的是密文而不是明文
        // if (password.equals(user.getPassword())){
        if (user != null && BCrypt.checkpw(password,user.getPassword())){
            // 密码匹配，登录成功
            // 创建用户令牌信息
            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("role","USER");
            tokenMap.put("success","SUCCESS");
            tokenMap.put("username",username);
            String token = JwtUtil.createJWT(UUID.randomUUID().toString(), JSON.toJSONString(tokenMap), null);

            // 把令牌信息存入到Cookie中
            Cookie cookie = new Cookie("Authorization",token);
            cookie.setDomain("localhost");  // 所属的域名
            cookie.setPath("/");            // 存储的路径

            response.addCookie(cookie);

            return new Result(true,StatusCode.OK,"登录成功了",token);
        }
        // 密码匹配失败，登录失败，提示账号或密码错误
        return new Result(false,StatusCode.LOGIN_ERROR,"帐号或密码错误");
    }

    /**
     * 查询全部数据
     * 只允许管理员admin角色访问，其他的角色无法访问
     * @return
     */
    @PreAuthorize("hasAnyRole('admin')")
    @GetMapping
    public Result findAll(){
        List<User> userList = userService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",userList) ;
    }

    /***
     * 根据ID查询数据
     * @param username
     * @return
     */

    @GetMapping("/{username}")
    public Result<User> findById(@PathVariable String username){
        User user = userService.findById(username);
        return new Result(true,StatusCode.OK,"查询成功",user);
    }

    /**
     * 加载用户的数据
     *
     * @param id
     * @return
     */
    @GetMapping("/load/{id}")
    public Result<User> findByUsername(@PathVariable(name = "id") String id) {
        //调用UserService实现根据主键查询User
        User user = userService.findById(id);
        return new Result<User>(true, StatusCode.OK, "查询成功", user);
    }


    /***
     * 新增数据
     * @param user
     * @return
     */
    @PostMapping
    public Result add(@RequestBody User user){
        userService.add(user);
        return new Result(true,StatusCode.OK,"添加成功");
    }


    /***
     * 修改数据
     * @param user
     * @param username
     * @return
     */
    @PutMapping(value="/{username}")
    public Result update(@RequestBody User user,@PathVariable String username){
        user.setUsername(username);
        userService.update(user);
        return new Result(true,StatusCode.OK,"修改成功");
    }


    /***
     * 根据ID删除品牌数据
     * @param username
     * @return
     */
    @DeleteMapping(value = "/{username}" )
    public Result delete(@PathVariable String username){
        userService.delete(username);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap){
        List<User> list = userService.findList(searchMap);
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
        Page<User> pageList = userService.findPage(searchMap, page, size);
        PageResult pageResult=new PageResult(pageList.getTotal(),pageList.getResult());
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }
}
