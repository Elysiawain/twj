package com.tangwuji.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangwuji.reggie.pojo.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
    //更新菜品状态
    @Select("update dish set status=#{status},update_user=#{updateUser},update_time=#{updateTime} where id = #{id}")
    void updateById(Long id, int status, String updateUser, LocalDateTime updateTime);
}
