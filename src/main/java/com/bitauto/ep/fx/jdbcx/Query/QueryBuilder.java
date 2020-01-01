package com.bitauto.ep.fx.jdbcx.Query;

public abstract class QueryBuilder extends GrammarBuilder
{
    private String tableName;



    private WhereBuilder whereBuilder;


    public void setTableName(String value){
        this.tableName = value;
    }

    public String getTableName(){
        return this.tableName;
    }

    public QueryBuilder(){

    }
    public QueryBuilder(String tablename){
        tableName = tablename;
    }


    /**
     * count语句
     *
     * @param sql
     * @return
     */
    public static String count(String sql) {
        StringBuilder countBuilder= new StringBuilder();
        countBuilder.append("select count(0) from ");

        countBuilder.append("(").append(sql).append(")").append(" query1111");
        return countBuilder.toString();
    }


    public WhereBuilder Where(){
        if(this.whereBuilder==null)
        {
           this.whereBuilder =new WhereBuilder();
        }
        return whereBuilder;
    }

    @Override
    public String Build(){
        clear();
        sqlBuilder.append("SELECT * FROM ")
                  .append(tableName).append(" WITH(NOLOCK)");
        if(whereBuilder!=null)
        {
            sqlBuilder.append(" WHERE 1=1  ")
                      .append(whereBuilder.Build());
            this.parameters.addAll(whereBuilder.parameters);
        }
        return sqlBuilder.toString();
    }

//    public PageBuilder Page(int startIndex,int pageSize,String orderBy){
//        return new PageBuilder(startIndex,pageSize,orderBy,this.Build());
//    }



}
