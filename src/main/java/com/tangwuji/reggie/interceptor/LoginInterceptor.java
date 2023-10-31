package com.tangwuji.reggie.interceptor;

import com.alibaba.fastjson.JSON;
import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@CrossOrigin(allowCredentials = "true",allowedHeaders="true")
//登录拦截器，主推
@Component//将该类下的方法交给IOC容器管理
public class LoginInterceptor implements HandlerInterceptor {
    //preHandle()拦截前方法，一般将判断多写在此方法中
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1、获取前端发来的请求数据，先获取URL
        StringBuffer requestURL = request.getRequestURL();
        String requestUrl = requestURL.toString();
        log.info("拦截请求URL为：{}",requestUrl);
/*        if (requestUrl.equals("http://localhost:8080/employee/page")){
            return true;
        }*/
        //3、如果不是请求登录，则校验jwt令牌
        //获取请求头中的jwt令牌
        String jwt = request.getParameter("token");
        log.info("前端登录令牌为：{}",jwt);
        if (jwt==null){
            response.getWriter().write(JSON.toJSONString(R.error("NOT_LOGIN")));
            return false;
        }

        //令牌存在，直接解析令牌，如果能够成功被解析并返回值，则登录成功程序放行，因为这里令牌解析错误会报错所以用try catch捕获异常
        try {
            Claims parseJwtResult = JwtUtil.parseJwt(jwt);
            Object subject = parseJwtResult.get("id");
            log.info("令牌解析信息为：{}",subject);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("解析令牌失败！");
            //解析失败
            response.getWriter().write(JSON.toJSONString(R.error("NOT_LOGIN")));
            return false;
        }

        // 用户已登录
        //已登录
        //令牌被正确解析，放行
        long ThreadId = Thread.currentThread().getId();
        log.info("当前线程ID为————：{}",ThreadId);
        log.info("令牌被正确解析，放行");
        return true;//返回为false拦截一切请求
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
