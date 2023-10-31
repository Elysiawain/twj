package com.tangwuji.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.mapper.CategoryMapper;
import com.tangwuji.reggie.pojo.Category;
import com.tangwuji.reggie.pojo.Dish;
import com.tangwuji.reggie.pojo.SetMeal;
import com.tangwuji.reggie.service.CategoryService;
import com.tangwuji.reggie.service.DishService;
import com.tangwuji.reggie.service.SetMealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 菜品分类
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetMealService setMealService;
    /**
     * 前端传来菜品id做相应判断后选择是否能够删除
     * @param ids
     */
    @Override
    public R<Object> deleteById(Long ids) {
        //线判断该菜品id下是否有关联的dish
        LambdaQueryWrapper<Dish>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId,ids);//添加查询条件
        long count = dishService.count(queryWrapper);//获取查询到的关联数，
        if (count>0){
            //有关联dish删除失败
            return R.error("该菜品分类下有关联菜品，请先删除关联菜品！");
        }
        //没有关联菜品，查询关联套餐，逻辑与上相同
        LambdaQueryWrapper<SetMeal> setMealQueryWrapper=new LambdaQueryWrapper<>();
        setMealQueryWrapper.eq(SetMeal::getCategoryId,ids);
        long countSetMeal = setMealService.count(setMealQueryWrapper);
        if (countSetMeal>0){
            //有关联套餐删除失败
            return R.error("该菜品分类下有关联套餐，请先删除关联套餐！");
        }
        //没有关联套餐，可以删除
        boolean flag = this.removeById(ids);
        if (flag){
            return R.success("删除成功！","success");
        }else {
            return R.error("删除失败");
        }
    }
}
