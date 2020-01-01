package com.bitauto.ep.fx.jdbcx.Query;

import com.bitauto.ep.fx.jdbcx.IPaginationSupport;
import com.bitauto.ep.fx.jdbcx.Pageination.MSSqlPaginationSupport;
import com.bitauto.ep.fx.jdbcx.Pageination.MySqlPaginationSupport;

public class PageBuilder extends GrammarBuilder
{
    private Integer pageSize = 0;
    private String orderBy;
    private Integer pageIndex = 0;
    private String pageSql = null;
    GrammarBuilder pvbuilder;
    private IPaginationSupport paginationSupport = null;


    //private GrammarBuilder grammarBuilder;

    public PageBuilder(String sqlType,int pageIndex,int pagesize,String orderby,String sql)
    {
        this.pageIndex = pageIndex;
        this.pageSize = pagesize;
        this.orderBy = orderby;
        this.pageSql = sql;
        paginationSupport = ObjectAbstractFactory.getPaginationSupport(sqlType);
    }

    private boolean isSqlType(String sqlType,String driverName){
        return sqlType.toUpperCase().contains(driverName);
    }

    @Override
    public void link(GrammarBuilder builder)
    {
        pvbuilder = builder;
    }

    @Override
    public String Build(){
        this.clear();
        if(pvbuilder!=null){
            this.parameters.addAll(pvbuilder.getParameters());
            this.pageSql = pvbuilder.toSql();
        }
        String sql =  paginationSupport.buildPaginationSql(
                this.pageSql,this.pageIndex,this.pageSize,this.orderBy);
        this.sqlBuilder.append(sql);
        return sql;
    }


}
