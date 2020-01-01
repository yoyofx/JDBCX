package com.bitauto.ep.fx.jdbcx.Pageination;

import com.bitauto.ep.fx.jdbcx.IPaginationSupport;

public class MySqlPaginationSupport implements IPaginationSupport {
    private static final String pageSQL = "select o.* from (%s) as o limit %d,%d";


    @Override
    public String buildPaginationSql(String sql, int pageIndex, int pageSize, String orderBy) {
        //pageIndex started 1 index;
        if (pageIndex <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("页数或页面数据量应该大于零");
        }
        int startIndex = (pageIndex - 1) * pageSize;

        return String.format(pageSQL, sql, startIndex, pageSize);
    }
}
