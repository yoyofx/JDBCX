package com.bitauto.ep.fx.jdbcx;

public interface IPaginationSupport
{
    String buildPaginationSql(String sql, int pageIndex, int pageSize,String orderby);
}
