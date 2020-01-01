package com.bitauto.ep.fx.jdbcx.Query;

public class QFunc
{
    private String sql;
    public QFunc(String sql){
        this.sql = sql;
    }

    public static QFunc eq(String sql){
        return new QFunc(sql);
    }

    @Override
    public String toString()
    {
        return this.sql;
    }
}
