package com.bitauto.ep.fx.jdbcx;


//import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 多数据源，集成druid
 * @author zhanglei13
 */
//@Configuration
//public class DataConfig {
//
//    @Bean(name = "wxappzwmc")
//    @Qualifier("wxappzwmc")
//    @Primary
//    @ConfigurationProperties(prefix = "spring.datasource.wxappzwmc")
//    public DataSource primaryDataSource(){
//        return DruidDataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "secondary")
//    @Qualifier("secondary")
//    @ConfigurationProperties(prefix = "spring.datasource.secondary")
//    public DataSource secondaryDataSource(){
//        return DruidDataSourceBuilder.create().build();
//    }
//
//}
