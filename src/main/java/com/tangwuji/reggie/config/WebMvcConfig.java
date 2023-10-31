package com.tangwuji.reggie.config;

import com.tangwuji.reggie.commons.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig  implements WebMvcConfigurer {
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器
        MappingJackson2HttpMessageConverter messageConverter=new MappingJackson2HttpMessageConverter();
        //设置转换器，替换掉默认
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //设置转换器顺序
        converters.add(1,messageConverter);
    }

    /**
     * 拓展mvc框架消息
     */

}
