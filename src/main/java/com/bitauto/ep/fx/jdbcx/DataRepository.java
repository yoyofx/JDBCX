package com.bitauto.ep.fx.jdbcx;

import org.springframework.stereotype.Repository;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repository
public @interface DataRepository  {
    String name() default "";
}