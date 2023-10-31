package com.tangwuji.reggie.config;


import com.tangwuji.reggie.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//配置类

@Configuration
public class LoginConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;
//跨域cors
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 添加允许的请求头
        registry.addMapping("/**")
                .allowedOrigins("http://127.0.0.1:5500") // 指定允许的域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 指定允许的请求方法
                .allowedHeaders("*") // 指定允许的请求头
                .allowCredentials(true) // 是否允许携带cookie
                .maxAge(3600); // 设置缓存时间
        WebMvcConfigurer.super.addCorsMappings(registry);
    }

    //注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {         //设置不需要被拦截的
        registry.addInterceptor(loginInterceptor).addPathPatterns("/employee/page","category/page","/dish/page","/order/page").excludePathPatterns(
                "/employee",
                "/employee/login",
               "/employee/logout"
        );
    }
}
