package com.tangwuji.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangwuji.reggie.pojo.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
