package com.tangwuji.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tangwuji.reggie.mapper.ShoppingCartMapper;
import com.tangwuji.reggie.pojo.ShoppingCart;
import com.tangwuji.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
