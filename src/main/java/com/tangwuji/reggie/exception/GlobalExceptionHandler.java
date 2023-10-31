package com.tangwuji.reggie.exception;

import com.tangwuji.reggie.commons.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

//全局异常处理器
@RestControllerAdvice//添加该注解就指定了当前类为全局异常处理类
public class GlobalExceptionHandler {
    //捕获数据库异常
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)//捕获所有异常,可指定捕获对象
    public R<Object> exceptions(Exception exception){
        exception.printStackTrace();
        return R.error("操作失败！");
    }
}
