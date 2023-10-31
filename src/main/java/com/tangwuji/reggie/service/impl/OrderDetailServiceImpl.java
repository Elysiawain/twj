package com.tangwuji.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tangwuji.reggie.mapper.OrderDetailMapper;
import com.tangwuji.reggie.pojo.OrderDetail;
import com.tangwuji.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
