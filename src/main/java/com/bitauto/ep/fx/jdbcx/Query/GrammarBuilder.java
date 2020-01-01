package com.bitauto.ep.fx.jdbcx.Query;

import java.util.ArrayList;
import java.util.List;

public abstract class GrammarBuilder
{
    protected StringBuilder sqlBuilder = new StringBuilder();

    protected List<Object> parameters = new ArrayList<Object>();


    /**
     * 获取参数列表对象
     * @return
     */
    public List<Object> getParameters() {
        return parameters;
    }


    /**
     * 情况组装器
     */
    public void clear() {

        this.sqlBuilder = new StringBuilder();

        this.parameters = new ArrayList<Object>();
    }


    public  void link(GrammarBuilder builder){}


    /**
     * 获取sql语句
     * @return
     */
    public String toSql() {
        //		if (reset) {
        //			clear();
        //		}
        //		reset = true;
        this.Build();
        return sqlBuilder.toString();
    }

    /**
     * 抽象组装接口
     */
    public abstract String Build();



}
