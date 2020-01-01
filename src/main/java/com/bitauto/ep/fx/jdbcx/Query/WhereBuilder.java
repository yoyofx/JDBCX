package com.bitauto.ep.fx.jdbcx.Query;

import java.util.ArrayList;
import java.util.List;

public class WhereBuilder extends GrammarBuilder
{
    private final List<QueryCondition> condtions = new ArrayList<QueryCondition>();
    private QueryMode mode;


    public WhereBuilder And(QueryCondition condition) {
        condition.setQueryMode(QueryMode.AND);
        condtions.add(condition);
        return this;
    }

    public WhereBuilder Or(QueryCondition condition) {
        condition.setQueryMode(QueryMode.OR);
        condtions.add(condition);
        return this;
    }

    @Override
    public void link(GrammarBuilder builder)
    {

    }

    @Override
    public String Build(){
        clear();
        int size = condtions.size();
        if (size > 0) {
            for (QueryCondition c : condtions) {
                String spliter = " OR ";
                if (c.getQueryMode() == QueryMode.AND) {
                    spliter = " AND ";
                }
                sqlBuilder.append(spliter).append(c.toSql());
                parameters.addAll(c.getValues());
            }
        }
        return sqlBuilder.toString();
    }


}
