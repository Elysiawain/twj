package com.tangwuji.reggie.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
@Slf4j
//此工具包旨在快速生成和解析jwt令牌
public class JwtUtil {
    private static String singKey="elysiawain";//设置密钥
    private static Date expiryTime=new Date(System.currentTimeMillis()+ 24 * 60 * 60 * 1000);//24个小时整,令牌有效期
    /*
    *
    * 生成令牌
    *
    * */
    public static String createJwt(Map<String,Object> claims){//需要返回值,返回值为String类型,有参数参数类型为map集;
        String jwt = Jwts.builder()
                .setExpiration(expiryTime)//设置令牌有效期
                .signWith(SignatureAlgorithm.HS256, singKey)//指定签名算法,一般选择HS256,第二个参数为签名密钥
                .addClaims(claims)//中间存储用户数据的部位可自定义(载荷)
                .compact();//将令牌转为字符串并返回,类型为String
        log.info("到期时间为：{}",expiryTime);
        return jwt;
    }

    /*
     *
     * 解析令牌
     *
     * */
    public static Claims parseJwt(String jwt){
        Claims claims = Jwts.parser()
                .setSigningKey(singKey)//之前的密钥
                .parseClaimsJws(jwt)
                .getBody();//将解析结果返回
        return claims;//返回解析结果
    }
}
