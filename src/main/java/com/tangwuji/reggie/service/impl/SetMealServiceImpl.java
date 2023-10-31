package com.tangwuji.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.dto.SetmealDto;
import com.tangwuji.reggie.mapper.SetMealMapper;
import com.tangwuji.reggie.pojo.SetMeal;
import com.tangwuji.reggie.pojo.SetmealDish;
import com.tangwuji.reggie.service.SetMealService;
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
public class SetMealServiceImpl extends ServiceImpl<SetMealMapper, SetMeal> implements SetMealService {
    @Autowired
    private SetmealDishService setmealDishService;//注入第二张表数据

    /**
     * 添加套餐
     * @param setmealDto
     * @return
     */
    @Override
    @Transactional
    public SetmealDto addSetMeal(SetmealDto setmealDto,Long updateId) {
        log.info("添加数据为{}",setmealDto);
        setmealDto.setCreateUser(updateId);
        setmealDto.setCreateTime(LocalDateTime.now());
        setmealDto.setUpdateUser(updateId);
        setmealDto.setUpdateTime(LocalDateTime.now());
        //先直接对原set_Meal表进行插入
        this.save(setmealDto);
        //从setmealDto中获取setmealDish
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //对setmealDishes进行遍历
        setmealDishes.stream().map(setMealDish ->{
            setMealDish.setSetmealId(setmealDto.getId());
            setMealDish.setCreateUser(updateId);
            setMealDish.setCreateTime(LocalDateTime.now());
            setMealDish.setUpdateUser(updateId);
            setMealDish.setUpdateTime(LocalDateTime.now());
            return setMealDish;
        }).collect(Collectors.toList());
        //将setmealDishes插入到setmealDish表中
        setmealDishService.saveBatch(setmealDishes);
        return setmealDto;
    }

    /**
     * 回写数据
     * @param ids
     * @return
     */
    @Override
    public SetmealDto getByIdWithDto(long ids) {
        //先直接查询套餐
        SetMeal setMeal = this.getById(ids);//查询完毕，查询setMeal_dish表中setmeal_id对应的name
        SetmealDto setmealDto=new SetmealDto();
        BeanUtils.copyProperties(setMeal,setmealDto);//将分支类拷贝进入父类中

        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setMeal.getId());//查询对应setMealId的dish
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        //添加进setMealDto中
        setmealDto.setSetmealDishes(setmealDishes);
        return setmealDto;
    }

    /**
     * 套餐更新
     * @param setmealDto
     * @param updateId
     */
    @Override
    public void updateSetMeal(SetmealDto setmealDto, long updateId) {
        //先直接更新setMeal表
        setmealDto.setUpdateUser(updateId);
        setmealDto.setUpdateTime(LocalDateTime.now());
        this.updateById(setmealDto);//setMeal表已经更新完成

        //在更新setMealDish时可以先将原先改setMealId对应的菜品进行删除
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);//对原先数据进行删除
        //获取setMealDish对象执行插入操作
        List<SetmealDish> setMealDishes = setmealDto.getSetmealDishes();//获取更新数据中的setMealDish数据
        //遍历setMealdishs添加对应数据
        setMealDishes.stream().map(setMealDish -> {
            setMealDish.setSetmealId(setmealDto.getId());//添加关联的setMealId
            setMealDish.setCreateUser(updateId);
            setMealDish.setCreateTime(LocalDateTime.now());
            setMealDish.setUpdateUser(updateId);
            setMealDish.setUpdateTime(LocalDateTime.now());
            return setMealDish;
        }).collect(Collectors.toList());
        //批量添加
        setmealDishService.saveBatch(setMealDishes);
        log.info("更新setMealDish成功！");
    }

    /**
     * 删除套餐
     * @param setMealList
     * @return
     */
    @Transactional//添加事务
    @Override
    public R<String> deleteWithDish(List<Long> setMealList) {
        //先判断改套是否能被删除，当该套餐正在起售中时不能被删除
        //先对setMeal表进行删除这里的list时套餐的id

        LambdaQueryWrapper<SetMeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(SetMeal::getId,setMealList);//添加查询条件
        //添加查询售卖状态的条件
        queryWrapper.eq(SetMeal::getStatus,1);//这里的值可以直接写死判断为1
        long count = this.count(queryWrapper);
        if (count!=0){
            //查询到不满足条件的套餐，该次删除失效
            return R.error("该套餐正在售卖中，不能被删除！");
        }

        //满足删除条件删除setMeal中的数据
        this.removeByIds(setMealList);
        //开始删除setMealDish中的向关数据，需要同过被删除的setMealId去查询出对应的dishes
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加过滤条件
        dishLambdaQueryWrapper.in(SetmealDish::getSetmealId,setMealList);
        setmealDishService.remove(dishLambdaQueryWrapper);//删除
        return R.success("删除套餐成功！","success");
    }

}
