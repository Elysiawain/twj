package com.tangwuji.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.dto.SetmealDto;
import com.tangwuji.reggie.pojo.SetMeal;

import java.util.List;

public interface SetMealService extends IService<SetMeal> {
    /**
     * 添加套餐
     * @param setmealDto
     * @return
     */
    SetmealDto addSetMeal(SetmealDto setmealDto,Long updateId);

    SetmealDto getByIdWithDto(long ids);

    void updateSetMeal(SetmealDto setmealDto, long updateId);

    /**
     * 删除套餐操作
     * @param setMealDtoList
     * @return
     */
    R<String> deleteWithDish(List<Long> setMealDtoList);
}
