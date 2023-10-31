package com.tangwuji.reggie.dto;


import com.tangwuji.reggie.pojo.Dish;
import com.tangwuji.reggie.pojo.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
//数据传输对象
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
