package com.bitauto.ep.fx.jdbcx;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 *  IOC容器获取Bean帮助类
 *  @author  zhanglei
 */

@Component("dbspi")
public class ServiceProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    private static ConfigurableApplicationContext configurableApplicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(ServiceProvider.applicationContext == null) {
            ServiceProvider.applicationContext = applicationContext;
        }
    }

    //获取applicationContext
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }




    //通过name获取 Bean.
    public static Object getService(String name){
        return getApplicationContext().getBean(name);
    }

    //通过class获取Bean.
    public static <T> T getService(Class<T> clazz){
        return getApplicationContext().getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getService(String name,Class<T> clazz){
        return getApplicationContext().getBean(name, clazz);
    }

}
