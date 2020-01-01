package com.bitauto.ep.fx.jdbcx.Query;

import com.bitauto.ep.fx.jdbcx.Helper.EntitySQLHelper;


public class EntityQueryBuilder<TEntity> extends QueryBuilder
{
    private Class<TEntity> entityClass;

    public EntityQueryBuilder(Class<TEntity> classsz)
    {
        String tableName = EntitySQLHelper.getTableName(classsz);
        this.entityClass = classsz;
        this.setTableName(tableName);
    }


    public Class<TEntity> getEntityClass()
    {
        return entityClass;
    }


}
