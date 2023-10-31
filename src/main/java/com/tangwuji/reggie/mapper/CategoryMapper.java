package com.tangwuji.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangwuji.reggie.pojo.Category;
import org.apache.ibatis.annotations.Mapper;
//菜品管理
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
