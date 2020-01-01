package com.bitauto.ep.fx.jdbcx.Helper;

import javax.persistence.Table;

public class EntitySQLHelper
{
    public static <T> String getTableName(Class<T> clazz){
        Table table = clazz.getAnnotation(Table.class);
        if(table != null){
            if(table.catalog() != null && table.catalog().length() > 0){
                return table.catalog() + "." + table.name();
            }else {
                return table.name();
            }
        }else {
            return clazz.getSimpleName();
        }
    }
}
