package com.tangwuji.reggie.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
@Configuration
public class RedisConfig  {
    @Resource
    private LettuceConnectionFactory lettuceConnectionFactory;
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        // 设置key序列化方式string
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // RedisSerializer.string() 等价于 new StringRedisSerializer()

        // 设置value的序列化方式json，使用GenericJackson2JsonRedisSerializer替换默认序列化
        //redisTemplate.setValueSerializer(RedisSerializer.json()); // RedisSerializer.json() 等价于 new GenericJackson2JsonRedisSerializer()

        // 设置hash的key的序列化方式
        redisTemplate.setHashKeySerializer(RedisSerializer.string());

        // 设置hash的value的序列化方式
        //redisTemplate.setHashValueSerializer(RedisSerializer.json());

        // 使配置生效
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
