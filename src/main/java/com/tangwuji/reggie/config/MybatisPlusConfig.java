package com.tangwuji.reggie.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

//配置MP的分页插件
@EnableTransactionManagement
@Configuration
@MapperScan("com.tangwuji.reggie.mapper")
public class MybatisPlusConfig {

    @Autowired
    private DataSource dataSource;
    //配置MP，如果不配置MP启动失效
    @Bean
    public MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean() {
        MybatisSqlSessionFactoryBean mybatisPlus = new MybatisSqlSessionFactoryBean();
        mybatisPlus.setDataSource(dataSource);
        mybatisPlus.setPlugins(mybatisPlusInterceptor());//如果不配置分页插件会失效
        return mybatisPlus;
    }
    @Bean//将其交给IOC容器管理
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        //创建MP拦截器
        MybatisPlusInterceptor mybatisPlusInterceptor=new MybatisPlusInterceptor();
        PaginationInnerInterceptor innerInterceptor=new PaginationInnerInterceptor();
        innerInterceptor.setDbType(DbType.MYSQL);
        innerInterceptor.setOverflow(true);
        //添加page插件
        mybatisPlusInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        mybatisPlusInterceptor.addInnerInterceptor(innerInterceptor);
        return mybatisPlusInterceptor;
    }
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return mybatisConfiguration -> mybatisConfiguration.setUseGeneratedShortKey(false);
    }



}
