package com.tangwuji.reggie.service;
//发送邮件
public interface EmailService {

    void sendSimpleMessage(String to, String subject, String text);

    void sendEmail(String email,String name);
}
