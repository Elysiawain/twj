package com.tangwuji.reggie.service.impl;

import com.tangwuji.reggie.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 描述：     EmailService实现类
 */
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSenderImpl javaMailSender;
    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("tangwuji20040212@163.com");
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(text);

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.send(simpleMailMessage);
    }

    public void sendEmail(String email,String code){
        //使用 JavaMailSender 创建一个 MIME 类型的信息对象实例。
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            //创建一个 MimeMessageHelper 对象实例，用于辅助构建 MIME 类型的信息。
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
            //设置邮件接收人的邮箱地址。
            helper.setTo(email);
            //设置邮件发送人的地址，此处应替换成真实的发送人邮箱地址。
            helper.setFrom("tangwuji20040212@163.com");
            helper.setSubject("菩提阁登录验证");
            Date date = new Date();
            //设置邮件的文本内容，其中使用了 HTML 标签来设置文本样式。具体来说，用了 <span> 标签来设置字体颜色，name 和 date 变量则是用于动态显示的内容。
            helper.setText("<span>你的验证码为：</span>"+code+"<br>请勿泄露给他人。<br>"+date+"<br> <img src=\"https://javaweb-twj.oss-cn-beijing.aliyuncs.com/elysiaHead.jpg\" alt=\"\" style=\"width: 200px;\">");

            //发送附件
            //helper.addAttachment("文件名",new File("路径"));
            //发送图片
           // helper.setText("内容<br><img src='Tets'>");
            //helper.addInline("测试图片",new FileSystemResource(new File("F:\\image\\elysiaHead.jpg")));
/*            //超链接
            helper.setText("内容<a href='http://www.baidu.com'>点我</a>",true);*/
            //调用 JavaMailSender 的 send() 方法来完成邮件发送操作，其中 mimeMessage 参数即为构建好的 MIME 类型信息对象。
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            //：捕获可能发生的异常并抛出运行时异常，以便能够及时处理问题。
            throw new RuntimeException(e);
        }
    }
}

