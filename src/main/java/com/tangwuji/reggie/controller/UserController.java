package com.tangwuji.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.pojo.User;
import com.tangwuji.reggie.service.EmailService;
import com.tangwuji.reggie.service.UserService;
import com.tangwuji.reggie.utils.EmailUtil;
import com.tangwuji.reggie.utils.JwtUtil;
import com.tangwuji.reggie.utils.ValidateCodeUtils;
import jakarta.mail.internet.AddressException;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
    //用户接口
    /**
     * 验证码
     */
    @PostMapping("/sendCode")
    public R<Object> senCode(@RequestBody User user, HttpSession httpSession){
        //获取手机号
        String phone = user.getPhone();
        //生成验证码
        if (StringUtils.isNotEmpty(phone)){
            //生成随机4位验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            log.info("生成的随机验证码为：{}",code);
            //调用阿里云短信api
            //SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);
            //将该code存入本次session
             httpSession.setAttribute("code",code);
        }
        return R.success(httpSession.getAttribute("code"),"success");
    }

    /**
     * 发送邮件
     * @param user
     * @return
     * @throws AddressException
     */
    @PostMapping("/sendEmail")
    @ResponseBody
    public R<Object> sendEmail(@RequestBody User user)

            throws AddressException {
        String email = user.getEmail();

        //检查邮件地址是否有效，检查是否已注册
        boolean validEmailAddress = EmailUtil.isValidEmailAddress(email);
        if(validEmailAddress) {
            boolean emailPassed = userService.checkEmailRegistered(email);
            if (!emailPassed) {
                log.info("该邮箱已注册");
            }
                //发送邮件
                String code = ValidateCodeUtils.generateValidateCode(6).toString();
                emailService.sendEmail(email,code);

                return R.success("发送成功！请前往邮箱查看");

        }else{
            return R.error("非法邮箱");
        }

    }

    //验证验证码，前端验证码携带在请求参数中
    @PostMapping("/login")
    public R<String> login(@RequestBody Map<String,String> map, HttpSession httpSession){
        Object checkCode = httpSession.getAttribute("code");
        log.info("校验验证码为：{}",checkCode);
        String code = map.get("code");
        log.info("验证码为：{}",code);
        String email = map.get("email");
        log.info("邮箱为：{}",email);


        //先判断用户填写的验证码是否正确，如果正确继续，错误返回错误信息
        if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(email)){
            //手机号和验证码无问题

            //判断该用户是否为新用户
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail,email);
            User checkUser = userService.getOne(queryWrapper);
            if (checkUser==null){
                //该用户为新用户，登录的同时执行注册
                User newUser=new User();
                newUser.setEmail(email);
                newUser.setName("邮箱用户"+email);
                userService.save(newUser);
            }
            Map<String,Object> keyMap=new HashMap<>();
            keyMap.put("id",email);
            //这里只存入姓名和年龄
            //调用方法,创建令牌
            String jwt = JwtUtil.createJwt(keyMap);
            log.info("令牌下发成功!");
            log.info("令牌为：{}",jwt);
            log.info("用户名：{}",checkUser.getName());
            if (checkUser.getName()==null){
                checkUser.setName("邮箱用户"+email);
            }
            String name = checkUser.getName();
            return R.success(name,jwt);//给前端返回jwt令牌，和用户数据
        }
        return R.error("登陆失败！");
    }

    @PostMapping("/loginout")
    public R<Object> logout(){
        return R.success();
    }

    /**
     * 获取用户数据，在用户进入用户主界面时调用
     * @return
     */
    @GetMapping("/info")
    public R<Object> getUser(String token){
        String uid = JwtUtil.parseJwt(token).get("id").toString();
        //根据uid查询用户的信息
        log.info("查询用户信息");
        User userInfo = userService.getById(uid);
        return R.success(userInfo);
    }
}
