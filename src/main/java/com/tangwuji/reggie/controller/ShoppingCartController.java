package com.tangwuji.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.pojo.ShoppingCart;
import com.tangwuji.reggie.service.ShoppingCartService;
import com.tangwuji.reggie.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping("/list")
    public R<List<ShoppingCart>> shoppingCartList(String token){
        if (token== null){
            return R.error("请先登录");
        }
        String id = JwtUtil.parseJwt(token).get("id").toString();
        log.info("当前查询账号数据为：{}",id);
        //查询shoppingCart表中对应uid的数据并返回
        LambdaQueryWrapper<ShoppingCart> query = new LambdaQueryWrapper<>();
        query.eq(ShoppingCart::getUserId,id);
        List<ShoppingCart> shoppingList = shoppingCartService.list(query);
        return R.success(shoppingList,"购物车列表");
    }

    /**
     * 添加购物车
     * @param shoppingCart
     * @param token
     * @return
     */
    @PostMapping("/add")
    public R<Object> addShopping(@RequestBody ShoppingCart shoppingCart,String token){
        log.info("添加购物车"+shoppingCart);
        log.info("token:{}",token);
        //解析令牌
        String uid = JwtUtil.parseJwt(token).get("id").toString();
        shoppingCart.setUserId(uid);//设置当前菜品的点餐用户
        //先查询当前菜品是否已经在购物车中，如果在就增加数量（口味要保持相同）
        shoppingCart.setCreateTime(LocalDateTime.now());
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        if (shoppingCart.getDishId()!= null)
        {
            //有菜品
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }
        else if(shoppingCart.getSetmealId()!= null){
            //有套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }else {
            //无任何套餐和菜品，请求错误
            return R.error("请求错误");
        }
        if (shoppingCart.getDishFlavor()!= null){
            queryWrapper.eq(ShoppingCart::getDishFlavor,shoppingCart.getDishFlavor());//确保口味也不一致
        }


        //查询是否有对应的数据
        ShoppingCart checkShoppingCart = shoppingCartService.getOne(queryWrapper);
        if (checkShoppingCart != null) {
         //存在同样规格的菜品
         //数量修改
            Integer number = checkShoppingCart.getNumber();//获取原有数据
            checkShoppingCart.setNumber(number+1);
            //更新

            shoppingCartService.updateById(checkShoppingCart);
        }else {
            //不存在相同的菜品，执行添加操作
            shoppingCartService.save(shoppingCart);
            checkShoppingCart=shoppingCartService.getById(shoppingCart.getId());
        }
        return R.success(checkShoppingCart);
    }

    /**
     * 修改数量（减少）
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<Object> subShoppingCart(@RequestBody ShoppingCart shoppingCart,String token){//前端传递对应的菜品id或setMealId
        String uid = JwtUtil.parseJwt(token).get("id").toString();
        //先判断是菜品还是套餐
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,uid);//添加对应的账号
        if (shoppingCart.getDishId()!=null){
            //修改菜品数量
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else if (shoppingCart.getSetmealId()!=null){
            //修改套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }else {
            return R.error("请求出错！");
        }
        //查询数据库中对应的数据
        ShoppingCart subShopping = shoppingCartService.getOne(queryWrapper);
        Integer number = subShopping.getNumber();
        //如果number-1为0表示当前菜品已经取消应该删除
        if (number-1<=0){
            shoppingCartService.removeById(subShopping.getId());
            //直接返回
            return R.success(subShopping);
        }
        subShopping.setNumber(number-1);
        //更新数据库
        shoppingCartService.updateById(subShopping);
        return R.success(subShopping);
    }

    @DeleteMapping("/clean")
    public R clean(String token){
        //获取修改人信息
        String uid = JwtUtil.parseJwt(token).get("id").toString();
        log.info("清空购物车账号id：{}",uid);
        //查询shoppingCart表中对应的数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.hasLength(uid),ShoppingCart::getUserId,uid);
        shoppingCartService.remove(queryWrapper);
        return R.success("购物车清空成功！");
    }
}
