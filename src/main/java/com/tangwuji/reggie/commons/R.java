package com.tangwuji.reggie.commons;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class R<T> implements Serializable {

    private Integer code; //状态码：1成功，0和其它数字为失败

    private String msg; //错误信息

    private T data; //数据，定义为泛型

    private Map map = new HashMap(); //动态数据

    //带数据返回的返回结果
    public static <T> R<T> success(T data,String msg) {
        R<T> r = new R<T>();
        r.msg=msg;
        r.data = data;
        r.code = 1;
        return r;
    }
    //带数据返回的返回结果
    public static <T> R<T> success(T data) {
        R<T> r = new R<T>();
        r.msg="success";
        r.data = data;
        r.code = 1;
        return r;
    }
    //不带数据返回的返回结果
    public static <T> R<T> success() {
        R<T> r = new R<T>();
        r.data = null;
        r.code = 1;
        return r;
    }

    //返回错误信息，不返回数据
    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
