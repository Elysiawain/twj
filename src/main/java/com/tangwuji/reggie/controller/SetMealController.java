package com.tangwuji.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.dto.SetmealDto;
import com.tangwuji.reggie.mapper.SetMealMapper;
import com.tangwuji.reggie.pojo.Category;
import com.tangwuji.reggie.pojo.SetMeal;
import com.tangwuji.reggie.service.CategoryService;
import com.tangwuji.reggie.service.SetMealService;
import com.tangwuji.reggie.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
/**
 * 套餐设置
 */
public class SetMealController {
    @Autowired
    private SetMealService setMealService;
    @Autowired
    private SetMealMapper setMealMapper;
    @Autowired
    private CategoryService categoryService;
    /**
     * 套餐分页查询
     */
    //分页查询（这里逻辑需要将菜品分类也一同查询出来）
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //本质是两次查询数据
        //创建分页器
        Page<SetMeal>setMealPage=new Page<>(page,pageSize);
        //构造setMealDto对象
        Page<SetmealDto> setMalDtoPage=new Page<>();

        //创建条件过滤器
        LambdaQueryWrapper<SetMeal> QueryWrapper=new LambdaQueryWrapper<>();
        if (name!=null){
            QueryWrapper.like(SetMeal::getName,name);
        }
        QueryWrapper.orderByDesc(SetMeal::getUpdateTime);
        setMealService.page(setMealPage,QueryWrapper);
        BeanUtils.copyProperties(setMealPage,setMalDtoPage,"records");//拷贝分支类，不拷贝records中的数据
        //取出setMeal中的records
        List<SetMeal> records = setMealPage.getRecords();
        //遍历添加进入setMealDto对象中
        List<SetmealDto> setMealDtoList = records.stream().map(setMeal -> {
            SetmealDto setmealDto = new SetmealDto();//每次循环添加
            BeanUtils.copyProperties(setMeal, setmealDto);//每次遍历需要拷贝原setMeal对象
            //在此遍历中需要将原有的setMeal中的records拷贝进入setMealDto对象中，先获取器对应的分类id(categoryId)
            Long categoryId = setMeal.getCategoryId();//或去要查询的分类Id,准备开始查询
            Category categoryName = categoryService.getById(categoryId);
            //判断分类id是否为空
            if (categoryName != null) {
                //有对应的套餐分类，开始写入
                setmealDto.setCategoryName(categoryName.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());//搜集转为list
        setMalDtoPage.setRecords(setMealDtoList);

        return R.success(setMalDtoPage,"success");
    }

    /**
     * 批量启用禁用
     */
    @CacheEvict(value = "setMeal",allEntries = true)//清除缓存
    @PostMapping("/status/{status}")
    public R<Object> updateSetMealDish(@PathVariable int status, String ids, String token) {

        log.info("jwt：{}", token);
        log.info("操作菜品id:{}", ids);
        String[] splitIds = ids.split(",");
        log.info("截取后长度为：{}", splitIds.length);
        Claims parseJwt = JwtUtil.parseJwt(token);
        Object upId = parseJwt.get("id");
       Long updateId = Long.parseLong(upId.toString());
        if (splitIds.length > 1) {
            List<Long> setMealIds = new ArrayList<>();
            for (String id : splitIds) {
                setMealIds.add(Long.parseLong(id));//添加进入list集合
            }
            //利用MP,进行数据批量查询修改
            //创建构造器
            LambdaQueryWrapper<SetMeal> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SetMeal::getId, setMealIds);
            List<SetMeal> setMealList = setMealMapper.selectList(queryWrapper);

            // 批量更新status值

             setMealList.forEach(dish -> {
                dish.setStatus(status);
                dish.setUpdateUser(Long.valueOf(updateId));
                dish.setUpdateTime(LocalDateTime.now());
            });
            // 使用LambdaUpdateChainWrapper进行批量更新
           LambdaUpdateChainWrapper<SetMeal> updateWrapper = new LambdaUpdateChainWrapper<>(setMealMapper);
            updateWrapper.in(SetMeal::getId, setMealList.stream().map(SetMeal::getId).collect(Collectors.toList()))
                    .set(SetMeal::getStatus, status)
                    .set(SetMeal::getUpdateUser, updateId)
                    .set(SetMeal::getUpdateTime, LocalDateTime.now())
                    .update();
            return R.success("修改成功！", "success");
        }
        LambdaQueryWrapper<SetMeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetMeal::getId,ids);//添加条件过滤器
        SetMeal setMealServiceById = setMealService.getById(ids);
        setMealServiceById.setUpdateTime(LocalDateTime.now());
        setMealServiceById.setUpdateUser(updateId);
        setMealServiceById.setStatus(status);
        log.info(splitIds.toString());
        log.info("执行起禁售操作");
        setMealService.updateById(setMealServiceById);
        return R.success("修改成功！", "success");
    }

    /**
     * 添加套餐
     */
    @CacheEvict(value = "setMeal",allEntries = true)//清除缓存
    @PostMapping
    public R<Object> addSetMeal(@RequestBody SetmealDto setmealDto, HttpServletRequest request) {
        String jwt = JwtUtil.parseJwt(request.getParameter("token")).get("id").toString();
        long updateId = Long.parseLong(jwt);
        //需要对两张数据表进行插入
        setMealService.addSetMeal(setmealDto,updateId);
        return R.success("添加成功！", "success");
    }
    /**
     * 套餐信息回写
     * @param id
     */
    @GetMapping("{id}")
    public R<SetmealDto> returnSetMealInfo( @PathVariable String id) {
        //前端传来的是该套餐的id，需要将其id对应的dish查找出来，查找setMeal_dish表中的dish
        log.info("前端请求套餐信息回写套餐id为：{}",id);
        long ids = Long.parseLong(id);
        SetmealDto setmealDto=setMealService.getByIdWithDto(ids);
        return R.success(setmealDto,"success");
    }

    @Cacheable(value = "setMeal",key = "#setMeal.categoryId")//开启缓存键不序列化，值序列化
    @GetMapping("/list")
    public R<List<SetMeal>> returnSetMealInfo( SetMeal setMeal) {
        //这个接口只需要回写套餐，不需要回写套餐的菜品
        //前端传来的是该套餐的id，需要将其id对应的dish查找出来，查找setMeal_dish表中的dish
        LambdaQueryWrapper<SetMeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(setMeal.getCategoryId()!=null,SetMeal::getCategoryId,setMeal.getCategoryId());
        queryWrapper.eq(setMeal.getStatus()!=null,SetMeal::getStatus,setMeal.getStatus());
        queryWrapper.orderByDesc(SetMeal::getUpdateTime);
        log.info("前端请求套餐信息回写套餐id为：{}",setMeal.getId());
        List<SetMeal> list = setMealService.list(queryWrapper);
        return R.success(list,"success");
    }
    /**
     * 套参修改
     * @param setmealDto
     * @param request
     * @return
     */
    @CacheEvict(value = "setMeal",allEntries = true)//清除缓存
    @PutMapping
    public R<Object> updateSetMeal(@RequestBody SetmealDto setmealDto, HttpServletRequest request) {
        //前端传来的数据为setMealDto类型，这里的更新涉及到更新setMeal表和setMeal_dish表
        //先直接将setMeal表更新，更新前获取跟新人更新时间等
        String jwt = JwtUtil.parseJwt(request.getParameter("token")).get("id").toString();
        long updateId = Long.parseLong(jwt);
        //更新可以不返回
        setMealService.updateSetMeal(setmealDto,updateId);
        log.info("前端请求套餐修改套餐信息为：{}",setmealDto.toString());
        return R.success("修改成功！", "success");
    }
    /**
     * 套餐的删除
     */
    @CacheEvict(value = "setMeal",allEntries = true)//清除缓存
    @DeleteMapping
    public R<Object> deleteSetMeal(String ids,String token ){
        String[] splitIds = ids.split(",");
        List<Long> idsList = new ArrayList<>();
        for (String splitId : splitIds) {
            idsList.add(Long.parseLong(splitId));
        }
        String operator = JwtUtil.parseJwt(token).get("id").toString();
        if (!operator.equals("370935549474770944")){
            //权限不足
            return R.error("你的权限不足！无法进行删除操作！");
        }
        //前端传递的数据可用集合去接收
        //该删除设计两张表的删除在删除前应该做相应的判断，在进入service层前我们可以判断一下执行人

        R<String> string = setMealService.deleteWithDish(idsList);
        if (string.getCode()==0){
            return R.error("删除失败！该套餐还在售卖中，请停售后再次尝试！");
        }
        return R.success("删除成功！", "success");
    }
}
