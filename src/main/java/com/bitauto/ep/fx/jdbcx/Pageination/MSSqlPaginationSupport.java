package com.bitauto.ep.fx.jdbcx.Pageination;

import com.bitauto.ep.fx.jdbcx.IPaginationSupport;

public class MSSqlPaginationSupport implements IPaginationSupport
{
    private static final String pageSQL = "select top %d o.* from (select row_number() over(order by %s) as rownumber,* from (%s) query1111 ) as o where rownumber > %d";


    @Override
    public String buildPaginationSql(String sql, int pageIndex, int pageSize,String orderBy)
    {
        //pageIndex started 1 index;
        if (pageIndex <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("页数或页面数据量应该大于零");
        }
        int startIndex = (pageIndex - 1) * pageSize;

        String paginationSql = String.format(pageSQL,pageSize,orderBy,sql,startIndex);
        return paginationSql;
    }
}
