package com.tangwuji.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.dto.DishDto;
import com.tangwuji.reggie.mapper.DishMapper;
import com.tangwuji.reggie.pojo.Category;
import com.tangwuji.reggie.pojo.Dish;
import com.tangwuji.reggie.pojo.DishFlavor;
import com.tangwuji.reggie.service.CategoryService;
import com.tangwuji.reggie.service.DishFlavorService;
import com.tangwuji.reggie.service.DishService;
import com.tangwuji.reggie.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//菜品管理
@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisTemplate redisTemplate;

    //分页查询（这里逻辑需要将菜品分类也一同查询出来）
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        //创建分页器
        Page<Dish> pageDish = new Page<>(page, pageSize);
        //创建dto对象准备进行拷贝
        Page<DishDto> dtoPage = new Page<>();
        //创建条件过滤器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        if (name != null) {
            queryWrapper.like(Dish::getName, name);//当name不为空时，模糊查询数据
        }
        queryWrapper.orderByDesc(Dish::getUpdateTime);//添加默认排序
        Page<Dish> pageInfo = dishService.page(pageDish, queryWrapper);
        //拷贝dish
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");//这里不拷贝records等会需要将其返回
        List<Dish> dishRecords = pageInfo.getRecords();
        //利用stream流设置dto
        List<DishDto> dishDtoList = dishRecords.stream().map(dish -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto); //拷贝dish
            //获取分类id，即将进行查询
            Long categoryId = dish.getCategoryId();
            //根据查询出的categoryId查询category对象
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();//从查询出的category对象中获取对应的值
            dishDto.setCategoryName(categoryName);//重新设置分类名称
            return dishDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(dishDtoList);//修改records

        return R.success(dtoPage, "success");
    }

    //菜品回写
    @GetMapping("{id}")
    public R<DishDto> returnDishInfo(@PathVariable String id) {
        //这里返回的数据比较全
        long returnId = Long.parseLong(id);
        DishDto dishDto =dishService.getByIdWithFlavor(returnId);
        return R.success(dishDto, "success");
    }

    //套餐菜品回写
    @GetMapping("/list")
    public R<List<DishDto>> returnDishList(Dish dishes) {
        long startTime = System.currentTimeMillis();
        String  categoryId = dishes.getCategoryId().toString();
        //将菜品信息写进redis缓存
        String key = "dish_" + categoryId;
        log.info("套餐菜品回写key——{}",key);
        List<DishDto> redisDishList= (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (redisDishList!=null&&redisDishList.size()>0){
            //缓存有值直接返回结果
            log.info("缓存有值直接返回结果");
            long redisEndTime = System.currentTimeMillis();
            log.info("走redis回写耗时——{}ms",(redisEndTime-startTime));
            return R.success(redisDishList);
        }//不存在继续以前步骤
        //在回写的时候应该将dish的口味也返回给前端
        log.info("套餐菜品回写id——{}",categoryId);
        //查询对应categoryId的dish,并回写
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(categoryId!=null,Dish::getCategoryId, categoryId);
        queryWrapper.eq(Dish::getStatus,1);//只查询在起售的菜品
        queryWrapper.orderByAsc(Dish::getSort).orderByAsc(Dish::getUpdateTime);//添加排序条件
        List<Dish> dishList = dishService.list(queryWrapper);


        //需要查询对应的口味
        //遍历子分支
        //利用stream流设置dto
        List<DishDto> dishDtoList = dishList.stream().map(dish -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto); //拷贝dish
            //获取分类id，即将进行查询
            Long categoryIds = dish.getCategoryId();
            //根据查询出的categoryId查询category对象
            Category category = categoryService.getById(categoryIds);
            String categoryName = category.getName();//从查询出的category对象中获取对应的值
            dishDto.setCategoryName(categoryName);//重新设置分类名称
            LambdaQueryWrapper<DishFlavor> flavorLambdaQueryWrapper=new LambdaQueryWrapper<>();//构造条件过滤器
            //根据dishId查询dishFlavor表
            flavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dish.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(flavorLambdaQueryWrapper);//获取flavors列表
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());
        //将菜品数据添加进入缓存
        long mySqlEndTime = System.currentTimeMillis();
        log.info("走MySql回写耗时——{}ms",(mySqlEndTime-startTime));
        redisTemplate.opsForValue().set(key,dishDtoList,1, TimeUnit.HOURS);
        return R.success(dishDtoList,"success");
    }

    /**
     * 菜品修改起禁售操作
     */
    @PostMapping("/status/{status}")
    public R<Object> updateDish(@PathVariable int status, String ids, String token) {

        Set<String> keys = stringRedisTemplate.keys("dish*");
        keys.forEach(key->redisTemplate.delete(key));
        log.info("jwt：{}", token);
        log.info("操作菜品id:{}", ids);
        String[] splitIds = ids.split(",");
        log.info("截取后长度为：{}", splitIds.length);
        Claims parseJwt = JwtUtil.parseJwt(token);
        Object upId = parseJwt.get("id");
        String updateId = upId.toString();
        if (splitIds.length > 1) {


            List<Long> dishIds = new ArrayList<>();
            for (String id : splitIds) {

                dishIds.add(Long.parseLong(id));//添加进入list集合
            }
            //利用MP,进行数据批量查询修改
            //创建构造器
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Dish::getId, dishIds);
            List<Dish> dishList = dishMapper.selectList(queryWrapper);

            // 批量更新status值

            dishList.forEach(dish -> {
                dish.setStatus(status);
                dish.setUpdateUser(Long.valueOf(updateId));
                dish.setUpdateTime(LocalDateTime.now());
            });
            // 使用LambdaUpdateChainWrapper进行批量更新
            LambdaUpdateChainWrapper<Dish> updateWrapper = new LambdaUpdateChainWrapper<>(dishMapper);
            updateWrapper.in(Dish::getId, dishList.stream().map(Dish::getId).collect(Collectors.toList()))
                    .set(Dish::getStatus, status)
                    .set(Dish::getUpdateUser, updateId)
                    .set(Dish::getUpdateTime, LocalDateTime.now())
                    .update();
            return R.success("修改成功！", "success");
        }
        log.info(splitIds.toString());
        log.info("执行起禁售操作");
        dishService.updateByIds(ids, status, updateId);

        return R.success("修改成功！", "success");
    }

    /**
     * 新增菜品的添加
     * 由于此添加需要操作两张数据表，需要用到dto
     */
    @Transactional
    @PostMapping
    public R<Object> addDish(@RequestBody DishDto dish, HttpServletRequest request) {
        String token = request.getParameter("token");
        Claims parseJwt = JwtUtil.parseJwt(token);
        Object updateId = parseJwt.get("id");
        long updateUser = Long.parseLong(updateId.toString());
        dish.setUpdateUser(updateUser);//添加更新人
        dish.setUpdateTime(LocalDateTime.now());
        dish.setCreateUser(updateUser);
        dish.setCreateTime(LocalDateTime.now());
        log.info("添加菜品信息为：{}", dish);
        //执行添加操作
        dishService.save(dish);
        //获取对应菜品id
        Long id = dish.getId();
        //将id重新写入
        List<DishFlavor> flavors = dish.getFlavors();
        flavors.stream().map(dishFlavor -> {
            dishFlavor.setDishId(id);
            dishFlavor.setUpdateUser(updateUser);
            dishFlavor.setUpdateTime(LocalDateTime.now());
            dishFlavor.setCreateUser(updateUser);
            dishFlavor.setCreateTime(LocalDateTime.now());
            return dishFlavor;
        }).collect(Collectors.toList());
        //执行更新flavor操作
        dishFlavorService.saveBatch(flavors);
        //删除菜品缓存
        Set<String> keys = stringRedisTemplate.keys("dish_*");
        keys.forEach(key->redisTemplate.delete(key));
        return R.success("添加成功", "success");
    }

    /**
     * 更新操作
     */
    @PutMapping
    public R<Object> update(@RequestBody DishDto dishDto, HttpServletRequest request) {
        Claims parseJwt = JwtUtil.parseJwt(request.getParameter("token"));
        long updateId = Long.parseLong(parseJwt.get("id").toString());
        dishService.updateDishDto(dishDto, updateId);
        //删除对应的菜品缓存
        String key = "dish_" + dishDto.getCategoryId();
        Boolean delete = redisTemplate.delete(key);
        if (delete){
            log.info("删除菜品缓存成功");
        }
        return R.success("更新成功", "success");
    }

    /**
     * 菜品的删除
     * @param ids
     */
    @DeleteMapping
    public R<Object> delete(@RequestParam("ids") String ids,String token){
        log.info("删除菜品对应的id为:{}",ids);
        String[] split = ids.split(",");
        //在删除这里做一个权限的校验
        String operatorId = JwtUtil.parseJwt(token).get("id").toString();
        if (!operatorId.equals("370935549474770944")){
            return R.error("删除失败，权限不足！");
        }
        List<Long> idsList = new ArrayList<>();
        for (String splitId : split) {
            idsList.add(Long.parseLong(splitId));
        }
        //先考虑这个删除能不能直接删除，很明显不能，如果当前菜品关联有套餐则不能删除，
        //查询是否有关联的套餐
        R<Object> checkDeleted=dishService.deletedWithDish(idsList);
        //删除对应的菜品缓存
        Set<String> keys = stringRedisTemplate.keys("dish_*");
        keys.forEach(key->redisTemplate.delete(key));
        return checkDeleted;
    }
}