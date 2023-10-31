package com.tangwuji.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tangwuji.reggie.pojo.User;

public interface UserService extends IService<User> {
     boolean checkEmailRegistered(String emailAddress);
}
