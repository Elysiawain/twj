package com.tangwuji.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tangwuji.reggie.mapper.OrdersMapper;
import com.tangwuji.reggie.pojo.Orders;
import com.tangwuji.reggie.service.OrdersService;
import org.springframework.stereotype.Service;

/**
 * 订单数据
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
}
