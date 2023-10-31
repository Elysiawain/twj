package com.tangwuji.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.dto.DishDto;
import com.tangwuji.reggie.mapper.DishMapper;
import com.tangwuji.reggie.pojo.Dish;
import com.tangwuji.reggie.pojo.DishFlavor;
import com.tangwuji.reggie.pojo.SetmealDish;
import com.tangwuji.reggie.service.DishFlavorService;
import com.tangwuji.reggie.service.DishService;
import com.tangwuji.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 更新状态
     * @param ids
     * @param status
     * @param updateId
     */
    @Override
    public void updateByIds(String ids, int status, String updateId) {
        //获取更新时间
        LocalDateTime  updateTime = LocalDateTime.now();
        Long id = Long.valueOf(ids);
        dishMapper.updateById(id,status,updateId,updateTime);
    }

    /**
     * 根据id查询菜品的全部信息进行回写（包括口味信息）
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //先查询菜品的信息
        Dish dish = this.getById(id);
        DishDto dishDto=new DishDto();
        BeanUtils.copyProperties(dish,dishDto);//将查询到的dish对象拷贝进dishDto对象中

        //在flavor中查询对应id的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());//查询对应菜品的口味信息
        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(dishFlavors);
        return dishDto;
    }

    /**
     * 更新菜品
     * @param dishDto
     * @param updateId
     * @return
     */
    @Transactional
    @Override
    public DishDto updateDishDto(DishDto dishDto, long updateId) {
        //先利用MP直接更新dish数据
        LambdaUpdateChainWrapper<Dish> updateWrapper = new LambdaUpdateChainWrapper<>(dishMapper);
        updateWrapper.in(Dish::getId,dishDto.getId())
                .set(Dish::getUpdateUser, updateId)
                .set(Dish::getUpdateTime, LocalDateTime.now())
                .set(Dish::getDescription,dishDto.getDescription())
                .set(Dish::getName,dishDto.getName())
                .set(Dish::getImage,dishDto.getImage())
                .set(Dish::getPrice,dishDto.getPrice())
                .set(Dish::getCategoryId,dishDto.getCategoryId())
                .update();
        //清理当前flavor表中数据
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());//匹配对象
        dishFlavorService.remove(queryWrapper);

        //重新将前端传来数据插入到flavor表中
        List<DishFlavor> dishFlavors = dishDto.getFlavors();
        dishFlavors=dishFlavors.stream().map(dishFlavor ->{
            dishFlavor.setDishId(dishDto.getId());
            dishFlavor.setUpdateTime(LocalDateTime.now());
            dishFlavor.setUpdateUser(updateId);
            dishFlavor.setCreateTime(LocalDateTime.now());
            dishFlavor.setCreateUser(updateId);
            return dishFlavor;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(dishFlavors);
        return  dishDto;
    }

    @Transactional
    @Override
    public R<Object> deletedWithDish(List<Long> idsList) {
        //先判断套餐setMealDish表中是否存在该类菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getDishId,idsList);
        if(setmealDishService.count(queryWrapper)>0){
            //查询出在售套餐中有该类菜品id
            return R.error("当前菜品已关联套餐，请先下架套餐");
        }
        //判断当前删除菜品是否是起售状态

        //无数据进行批量删除
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getStatus,1);
        dishLambdaQueryWrapper.in(Dish::getId,idsList);//查找对应的DishId进行删除
        long checkStatus = this.count(dishLambdaQueryWrapper);
        if (checkStatus >1){
            return R.error("当前菜品已上架，请先下架");
        }
        this.removeByIds(idsList);
        return R.success("删除成功","success");
    }
}
