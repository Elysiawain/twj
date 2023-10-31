package com.tangwuji.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tangwuji.reggie.mapper.UserMapper;
import com.tangwuji.reggie.pojo.User;
import com.tangwuji.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
        @Autowired
        private UserMapper userMapper;
    @Override
    public boolean checkEmailRegistered(String emailAddress) {//查询数据库中是否有该邮箱信息
        log.info("emailAddress:{}",emailAddress);
        LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail,emailAddress);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {//该用户存在
            return false;
        }
        return true;
    }
}
