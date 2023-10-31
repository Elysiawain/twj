package com.tangwuji.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.dto.DishDto;
import com.tangwuji.reggie.pojo.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    void updateByIds(String ids, int status, String updateId);

    DishDto getByIdWithFlavor(Long id);

    /**
     * 更新菜品
     * @param dish
     * @param updateId
     * @return
     */
    DishDto updateDishDto(DishDto dish, long updateId);

    /**
     * 菜品删除
     * @param idsList
     * @return
     */
   R<Object> deletedWithDish(List<Long> idsList);
}
