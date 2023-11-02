package com.tangwuji.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.pojo.Category;
import com.tangwuji.reggie.service.CategoryService;
import com.tangwuji.reggie.service.DishService;
import com.tangwuji.reggie.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
        @Autowired
        private CategoryService categoryService;
        @Autowired
        private DishService dishService;

    /**
     * 新增菜品
     */
    @PostMapping
    public R<Object> addCategory(HttpServletRequest request, @RequestBody Category category){//前端传递JSON数据进行封装
        String jwt = request.getParameter("token");
        //解析令牌
        Claims parseJwt = JwtUtil.parseJwt(jwt);
        Object updateId =  parseJwt.get("id");
        log.info("新增菜品信息：{}，更新人id:{}",category,updateId);
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateTime(LocalDateTime.now());
        category.setCreateUser(Long.valueOf(updateId.toString()));
        category.setUpdateUser(Long.valueOf(updateId.toString()));
        //利用MP
        categoryService.save(category);
        return R.success("添加成功！","success");
    }

    /**
     * 菜品分页查询
     */
    @GetMapping("/page")//此请求需要拦截
    public R<Page> page(int page, int pageSize){
        //构造分页器
        log.info("当前页：{}，每页展示最大数：{}",page,pageSize);
        Page<Category> getPage = new Page(page,pageSize);
        //构造条件过滤器
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.orderByDesc(Category::getUpdateTime);
        //查找
        Page<Category> pageInfo = categoryService.page(getPage, queryWrapper);

        return R.success(pageInfo,"success");
    }

    /**
     * 删除菜品
     */
    @DeleteMapping
    public R<Object> delete(Long ids){
        log.info("要删除的菜品id：{}",ids);
        //在删除时应该进行判断，该菜品是否有子菜品，如果有就提示删除失败
        R<Object> deletedResult = categoryService.deleteById(ids);
        //categoryService.removeById(ids);
        return deletedResult;
    }

    /**
     * 更新菜品
     */
    @PutMapping
    public R<Object> update(@RequestBody  Category category,HttpServletRequest request){
        log.info("更新菜品信息为：{}",category.toString());
        //判断该菜品或套餐是否已存在
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.eq(Category::getName,category.getName());
        //如果重复会抛给全局异常处理器，返回重复添加错误信息
        //设置更新时间，和更新人
        category.setUpdateTime(LocalDateTime.now());
        String jwt = request.getParameter("token");
        String username = JwtUtil.parseJwt(jwt).get("id").toString();
        log.info("更新人id：{}",username);
        category.setUpdateUser(Long.valueOf(username));
        categoryService.updateById(category);
        return R.success("更新成功！","success");
    }

    /**
     * dish调用菜品type
     */
    @GetMapping("/list")
        public R<List<Category>> categoryList(Category category,String token){
        if (token== null){
            return R.error("请先登录");
        }

        String uid = JwtUtil.parseJwt(token).get("id").toString();
        log.info("用户id：{}",uid);//该方法只有用户端才会调用
        log.info("菜品列表请求");
            //这段代码的作用是查询数据库中 "category" 表中 "type" 字段等于 "type" 的所有记录，并将结果存储在 "categoryList" 列表中。
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
            queryWrapper.orderByAsc(Category::getSort).
                    orderByDesc(Category::getUpdateTime);
        List<Category> categoryList = categoryService.list(queryWrapper);
        return R.success(categoryList,"success");
    }



}
