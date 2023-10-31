package com.tangwuji.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.pojo.*;
import com.tangwuji.reggie.service.*;
import com.tangwuji.reggie.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@Slf4j
@Transactional
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDetailService orderDetailService;
    @PostMapping("/submit")
    public R<Object> submit(String remark,String addressBookId,Integer payMethod,String token) {//前端提交数据封装进入Orders对象中
        String uid = JwtUtil.parseJwt(token).get("id").toString();
        log.info("操作对象账号为：{}",uid);
        //先根据uid查找其对应的购物车
        LambdaQueryWrapper<ShoppingCart> shoppingCart=new LambdaQueryWrapper<>();
        shoppingCart.eq(ShoppingCart::getUserId,uid);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCart);//该账号对应的购物车数据列表
        //查询用户数据
        LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail,uid);
        User user=userService.getOne(queryWrapper);
        //查询地址数据
        AddressBook addressBook = addressBookService.getById(addressBookId);
        long id = IdWorker.getId();
        //统计金额，同时将订单详细完成
        AtomicInteger amount=new AtomicInteger();//原子性计算
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(s -> {
            //添加OrderDetail属性
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(id);
            orderDetail.setNumber(s.getNumber());
            orderDetail.setAmount(s.getAmount());
            orderDetail.setDishFlavor(s.getDishFlavor());
            orderDetail.setDishId(s.getDishId());
            orderDetail.setImage(s.getImage());
            orderDetail.setSetmealId(s.getSetmealId());
            orderDetail.setName(s.getName());
            amount.addAndGet(s.getAmount().multiply(new BigDecimal(s.getNumber())).intValue());//统计金额

            return orderDetail;
        }).collect(Collectors.toList());
        //设置属性
        Orders orders=new Orders();
        orders.setAddressBookId(Long.parseLong(addressBookId));
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setPayMethod(payMethod);
        orders.setRemark(remark);
        orders.setUserId(uid);
        orders.setId(id);
        orders.setStatus(1);
        orders.setNumber(String.valueOf(id));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setAddress
                ((addressBook.getProvinceName()== null?"" : addressBook. getProvinceName ())
                        + (addressBook. getCityName() == null ?"" : addressBook. getCityName ())
                        + (addressBook. getDistrictName() == null ? "": addressBook. getDistrictName ())
                                + (addressBook. getDetail() == null ? "": addressBook. getDetail())) ;
        ordersService.save(orders);
        log.info("对象为：{}",orders);

        //将数据添加进oderDetail中number = "1718513516237684738"
        orderDetailService.saveBatch(orderDetailList);
        shoppingCartService.remove(shoppingCart);
        return R.success();
    }

    @GetMapping("/page")
    public R<Object> page(int page,int pageSize){
        Page<Orders> ordersPage=new Page<>(page,pageSize);
        ordersService.page(ordersPage);
        return R.success(ordersPage);
    }

    @GetMapping("/userPage")
    public R<Object> userPage(int page,int pageSize,String token){
        String uid = JwtUtil.parseJwt(token).get("id").toString();
        Page<Orders> ordersPage=new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,uid);
        ordersService.page(ordersPage,queryWrapper);
        return R.success(ordersPage);
    }
}
