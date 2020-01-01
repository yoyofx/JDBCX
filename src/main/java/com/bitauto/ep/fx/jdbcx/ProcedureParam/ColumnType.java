package com.bitauto.ep.fx.jdbcx.ProcedureParam;

import org.springframework.stereotype.Repository;

import java.lang.annotation.*;
import java.sql.Types;

/**
 * @ProjectName: com.bitauto.ep.fx
 * @Package: com.bitauto.ep.fx.repositorys.BaseRepository.ProcedureParam
 * @ClassName: ColumnType
 * @Description: 声明字段在sql server中的字段类型 默认INTEGER
 * @Author: xw
 * @CreateDate: 2018/5/3 11:12
 */

@Documented
@Repository
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ColumnType
{
    int value() default Types.INTEGER;
}
