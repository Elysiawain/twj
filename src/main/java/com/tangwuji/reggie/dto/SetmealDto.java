package com.tangwuji.reggie.dto;


import com.tangwuji.reggie.pojo.SetMeal;
import com.tangwuji.reggie.pojo.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends SetMeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
