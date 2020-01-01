package com.bitauto.ep.fx.jdbcx.ProcedureParam;

import org.springframework.stereotype.Repository;

import java.lang.annotation.*;

/**
 * @ProjectName: fx
 * @Package: com.bitauto.ep.fx.jdbcx.ProcedureParam
 * @ClassName: NoConverColumn
 * @Description: 表明这个字段不需要转换
 * @Author:
 * @CreateDate: 2018/5/14 15:18
 */
@Documented
@Repository
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface NoConverColumn
{
    int value() default 0;
}
