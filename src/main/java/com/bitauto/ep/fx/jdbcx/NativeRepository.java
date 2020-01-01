package com.bitauto.ep.fx.jdbcx;

import com.bitauto.ep.fx.jdbcx.BeanMapper.BeanHandler;
import com.bitauto.ep.fx.jdbcx.BeanMapper.BeanListHandler;
import com.bitauto.ep.fx.jdbcx.Pageination.PageResult;
import com.bitauto.ep.fx.jdbcx.ProcedureParam.ParamEntity;
import com.bitauto.ep.fx.jdbcx.ProcedureParam.TypeEnum;
import com.bitauto.ep.fx.jdbcx.Provider.SqlServerCallableStatementInput;
import com.bitauto.ep.fx.jdbcx.Query.*;
//import com.bitauto.ep.fx.utils.ioc.ServiceProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * JDBC原生仓库，用于简化数据库访问API
 */
public abstract class NativeRepository
{
    /**
     * 查询指定SQL语句返回的List实体，SqlParameterSource为参数源。
     */
    private <TEntity> List<TEntity> Query(String sql, SqlParameterSource parameterSource, Class<TEntity> clazz)
    {
        //RowMapper<TEntity> automapper = RowMapperFactory.getRowMapper(clazz);
        return this.getNamedDB().query(sql, parameterSource, new BeanListHandler<>(clazz));
    }

    /**
     *  查询指定sql语句返回的实体列表
     * @param sql SQL语句,such as: select * from t1 where id=:id
     * @param paramObject 自定义对象(实体)参数 ,such as :class p { id = "1" }
     * @param clazz 返回类型的class
     * @param <TEntity> 返回类型
     * @return List<TEntity>
     */
    protected <TEntity> List<TEntity> Query(String sql, Object paramObject, Class<TEntity> clazz)
    {
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(paramObject);
        return this.Query(sql, parameterSource, clazz);
    }

    /**
     *  查询指定sql语句返回的实体列表
     * @param sql SQL语句,such as: select * from t1 where id=:id
     * @param paramList Map类型的参数集合, such as :(key="id" , value=?)
     * @param clazz 返回类型的class
     * @param <TEntity> 返回类型
     * @return List<TEntity>
     */
    public <TEntity> List<TEntity> Query(String sql, HashMap paramList, Class<TEntity> clazz)
    {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource(paramList);
        return this.Query(sql, parameterSource, clazz);
    }

    /**
     *  查询指定sql语句返回的实体列表
     * @param key sql map 中的key
     * @param paramList Map类型的参数集合, such as :(key="id" , value=?)
     * @param clazz 返回类型的class
     * @param <TEntity> 返回类型
     * @return List<TEntity>
     */
    public <TEntity> List<TEntity> QueryBySqlMap(String key, HashMap paramList, Class<TEntity> clazz)
    {
       return this.Query(sqlReader.getDynamicalSql(key,paramList),paramList,clazz);
    }

    public <TEntity> List<TEntity> queryForList(QueryBuilder queryBuilder,Class<TEntity> clazz){
        String sql = queryBuilder.Build();
        return  getNormalDB().query(sql,queryBuilder.getParameters().toArray(),new BeanListHandler<>(clazz));
    }

    public <TEntity> TEntity queryForEntity(EntityQueryBuilder queryBuilder,Class<TEntity> clazz){
        String sql = queryBuilder.Build();
        return getNormalDB().query(sql,queryBuilder.getParameters().toArray(),new BeanHandler<>(clazz));
    }

    /**
     *
     * @param sql
     * @param paramList 参数
     * @param pageIndex 第几页
     * @param pageSize 页大少
     * @param orderBy 排序字段
     * @param clazz   返回类型
     * @param <TEntity>
     * @return
     * @throws SQLException
     */
    public <TEntity> PageResult<TEntity> getPageResult(String sql,HashMap paramList,int pageIndex,int pageSize,String orderBy,Class<TEntity> clazz) throws SQLException
    {
        PageResult<TEntity> result = null;

        String countSQL = QueryBuilder.count(sql);
        int recordCount = this.getNamedDB().queryForObject(countSQL,paramList,Integer.class);

        PageBuilder pageBuilder = new PageBuilder(this.getSqlType(),pageIndex,pageSize,orderBy,sql);
        String pageSQL = pageBuilder.toSql();
        List<TEntity> dataList = this.Query(pageSQL,paramList,clazz);

        result = new PageResult<>(dataList,pageIndex,pageSize,recordCount);

        return result;

    }

    /**
     *
     * @param key sql xml中的key
     * @param paramList 参数
     * @param pageIndex 第几页
     * @param pageSize 页大少
     * @param orderBy 排序字段
     * @param clazz   返回类型
     * @param <TEntity>
     * @return
     * @throws SQLException
     */
    public <TEntity> PageResult<TEntity> getPageResultBySqlMap(String key,HashMap paramList,int pageIndex,int pageSize,String orderBy,Class<TEntity> clazz) throws SQLException
    {
       return this.getPageResult( sqlReader.getDynamicalSql(key,paramList),paramList,pageIndex,pageSize,orderBy,clazz);
    }


    public Map QueryForProc(String paramName, final List<ParamEntity> procedureList)
    {
        return getProcedureData(paramName, procedureList);
    }


    public XDataSet QueryForProc(ProcQueryBuilder queryBuilder){
        Map map = this.QueryForProc(queryBuilder.getProcName(),queryBuilder.getParameters());
        return new XDataSet(map);
    }

    /**
     *  查询指定sql语句返回的实体列表
     * @param sql SQL语句,such as: select * from t1 where id=?
     * @param params Map类型的参数集合, such as : new Object[]{ "10001" }
     * @param clazz 返回类型的class
     * @param <TEntity> 返回类型
     * @return List<TEntity>
     */
    protected <TEntity> List<TEntity> Query(String sql, Object[] params, Class<TEntity> clazz)
    {
        return this.getNormalDB().query(sql, params, new BeanListHandler<>(clazz));
    }

    /**
     *  查询指定sql语句返回的实体列表
     * @param sql SQL语句,such as: select * from t1 where id=:id
     * @param paramList Map类型的参数集合, such as :(key="id" , value=?)
     * @param clazz 返回类型的class
     * @param <TEntity> 返回类型
     * @return TEntity
     */
    public <TEntity> TEntity QueryForObject(String sql, HashMap paramList, Class<TEntity> clazz)
    {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource(paramList);
        //RowMapper<TEntity> automapper = RowMapperFactory.getRowMapper(clazz);
        TEntity ret = null;
        try
        {
            ret = this.getNamedDB().query(sql, parameterSource, new BeanHandler<>(clazz));
        }
        catch (EmptyResultDataAccessException ex){
            ret = null;
        }
        return ret;
    }

    /**
     *  查询指定sql语句返回的实体列表
     * @param key SQL语句,sql xml 中的key
     * @param paramList Map类型的参数集合, such as :(key="id" , value=?)
     * @param clazz 返回类型的class
     * @param <TEntity> 返回类型
     * @return TEntity
     */
    public <TEntity> TEntity QueryForObjectBySqlMap(String key, HashMap paramList, Class<TEntity> clazz)
    {
        return this.QueryForObject(  sqlReader.getDynamicalSql(key,paramList),paramList,clazz);
    }


    public <TEntity> TEntity QueryForObject(String sql, Object[] paramList, Class<TEntity> clazz){
        TEntity entity = this.getNormalDB().queryForObject(sql,paramList,clazz);
        return entity;
    }

    /**
     *  查询指定sql语句返回的实体列表
     * @param sql SQL语句,such as : select count(*) from t1 where id=:id
     * @param paramList Map类型的参数集合, such as :(key="id" , value=?)
     * @return 返回单值,such as : (Integer) QueryForSingle(....)
     */
    protected Object QueryForSingle(String sql, HashMap paramList)
    {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource(paramList);
        return this.getNamedDB().queryForObject(sql, parameterSource, Object.class);
    }

    protected Object QueryForSingleBySqlMap(String key, HashMap paramList)
    {
       return this.QueryForSingle(sqlReader.getDynamicalSql(key,paramList),paramList);
    }


    /**
     *  执行存储过程并返回
     * @param sql such as : exec 存储过程名
     * @param lst (key="id" , value=?)
     * @return List<Map<String, Object>>
     */
    protected List<Map<String, Object>> executeProc(String sql, HashMap lst)
    {
        return this.getNamedDB().queryForList(sql, lst);
    }


    private Map getProcedureData(String paramName, final List<ParamEntity> procedureList)
    {
        Map returnMap = (Map) this.getNormalDB().execute((ConnectionCallback) conn ->
        {
            //生成存储过程调用字符串
            StringBuffer buf = new StringBuffer();
            buf.append("{call ");
            buf.append(paramName);
            buf.append("(");

            procedureList.stream().
                    filter(s -> !s.getType().equals(TypeEnum.OUTTABLE)).
                    forEach(s -> buf.append("?,"));


            if (procedureList.size() > 0)
            {
                buf.deleteCharAt(buf.length() - 1);
            }

            buf.append(")}");

            System.out.println("callStr:" + buf.toString());
            //String sqlType = conn.getMetaData().getDriverName();

            CallableStatement cstmt = conn.prepareCall(buf.toString());

            for (int j = 0; j < procedureList.size(); j++)
            {
                ParamEntity entity = procedureList.get(j);

                TypeEnum paramType = entity.getType();

                Object dataValue = entity.getValue();

                int parameterIndex = j + 1;

                switch (paramType)
                {
                    case INPUT:
                        InputParam(cstmt, dataValue, parameterIndex, entity.getName());
                        break;
                    case OUTPUT:
                        OutputParam(cstmt, dataValue, parameterIndex);
                        break;
                    default:
                        break;
                }
            }
            //返回的map
            Map<String, Object> returnMap1 = new HashMap<>();
            boolean result = cstmt.execute();

            Integer k = 0;
            while (true)
            {
                if (result)
                {
                    ResultSet rs = cstmt.getResultSet();
                    returnMap1.put(k.toString(), ConverResultToList(rs));
                    k++;
                }
                else
                {
                    if (cstmt.getUpdateCount() == -1)
                    {
                        break;
                    }
                }

                result = cstmt.getMoreResults();
            }

            for (int j = 0; j < procedureList.size(); j++)
            {
                ParamEntity entity = procedureList.get(j);
                if (entity.getType().equals(TypeEnum.OUTPUT))
                {
                    //说明此函数是返回函数
                    if (entity.getValue() instanceof String)
                    {
                        returnMap1.put(entity.getName(), cstmt.getString(j + 1));
                    }
                    else if (entity.getValue() instanceof Integer)
                    {
                        returnMap1.put(entity.getName(), cstmt.getInt(j + 1));
                    }
                    else if (entity.getValue() instanceof Double)
                    {
                        returnMap1.put(entity.getName(), cstmt.getDouble(j + 1));
                    }
                    else if (entity.getValue() instanceof Date)
                    {
                        returnMap1.put(entity.getName(), cstmt.getDate(j + 1));
                    }
                }
            }

            cstmt.close();

            return returnMap1;
        });
        return returnMap;
    }


    /**
     * 构建返回参数
     *
     * @param cstmt
     * @param dataValue
     * @param parameterIndex
     * @throws SQLException
     */
    private void OutputParam(CallableStatement cstmt, Object dataValue, int parameterIndex) throws SQLException
    {
        if (dataValue instanceof String)
        {
            cstmt.registerOutParameter(parameterIndex, Types.CHAR);
        }
        else if (dataValue instanceof Integer)
        {
            cstmt.registerOutParameter(parameterIndex, Types.INTEGER);
        }
        else if (dataValue instanceof Long)
        {
            cstmt.registerOutParameter(parameterIndex, Types.BIGINT);
        }
        else if (dataValue instanceof Double)
        {
            cstmt.registerOutParameter(parameterIndex, Types.DECIMAL);
        }
        else if (dataValue instanceof Date)
        {
            cstmt.registerOutParameter(parameterIndex, Types.DATE);
        }
    }

    /**
     * 构建传入参数
     *
     * @param cstmt
     * @param dataValue
     * @param parameterIndex
     * @throws SQLException
     */
    private void InputParam(CallableStatement cstmt, Object dataValue, int parameterIndex, String paramName) throws SQLException
    {
        try
        {
            //说明此函数是传入函数
            if (dataValue instanceof String){
                cstmt.setString(parameterIndex, dataValue.toString());
            }
            else if (dataValue instanceof Integer){
                cstmt.setInt(parameterIndex, Integer.parseInt(dataValue.toString()));
            }
            else if (dataValue instanceof Long) {
                    cstmt.setLong(parameterIndex, Long.parseLong(dataValue.toString()));
            }
            else if (dataValue instanceof Double) {
                    cstmt.setDouble(parameterIndex, Double.parseDouble(dataValue.toString()));
            }
            else if (dataValue instanceof Date) {
                try
                {
                    DateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    cstmt.setDate(parameterIndex, new Date(formatter1.parse(dataValue.toString()).getTime()));
                }
                catch (Exception e1)
                {
                    DateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
                    cstmt.setDate(parameterIndex, new Date(formatter2.parse(dataValue.toString()).getTime()));
                }
            }
            ObjectAbstractFactory.addInputParameter(getSqlType(),cstmt, dataValue, parameterIndex, paramName);

        }
        catch (Exception e)
        {
            throw new SQLException(dataValue.getClass().getSimpleName()+  "数据类型转换错误,value="+ dataValue );
        }

    }

    /**
     *  ResultSet to List
     * @param rs
     * @return
     * @throws SQLException
     */
    private static List ConverResultToList(ResultSet rs) throws SQLException
    {
        List ls = new ArrayList();
        while (rs.next())
        {
            Map vc = new LinkedHashMap();
            for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++)
            {
                vc.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
            }
            ls.add(vc);
        }
        return ls;
    }


    /**
     *  执行原生SQL,非查询！
     * @param sql SQL语句,such as: delete from t1 where id=?
     * @param params Map类型的参数集合, such as : new Object[]{ "10001" }
     * @return 影响行数
     */
    protected int executeRawSql(String sql, Object[] params)
    {
        return this.getNormalDB().update(sql, params);
    }

    protected long  UpdateKey(String sql, HashMap paramList)
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        long  autoIncId = 0;
        MapSqlParameterSource parameterSource = new MapSqlParameterSource(paramList);
        this.getNamedDB().update(sql, parameterSource,keyHolder,new String[]{ "id"  });
        autoIncId = keyHolder.getKey().longValue();
        return autoIncId;
    }

    public long  UpdateKeyBySqlMap(String key, HashMap paramList){
        return  this.UpdateKey( sqlReader.getDynamicalSql(key,paramList) ,paramList);
    }

    public Object  UpdateBySqlMap(String key, HashMap paramList){
        return  this.Update( sqlReader.getDynamicalSql(key,paramList) ,paramList);
    }

    protected Object Update(String sql, HashMap paramList)
    {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource(paramList);
        return this.getNamedDB().update(sql, parameterSource);
    }

    protected NamedParameterJdbcTemplate getNamedDB()
    {
        DataSource dataSource = ServiceProvider.getService(this.dataSourceName, DataSource.class);
        this.dataSource = dataSource;
        return new NamedParameterJdbcTemplate(dataSource);
    }

    protected JdbcTemplate getNormalDB()
    {
        DataSource dataSource = ServiceProvider.getService(this.dataSourceName, DataSource.class);
        this.dataSource = dataSource;
        return new JdbcTemplate(dataSource);
    }

    protected String getSqlType() throws SQLException
    {
        String sqlType = this.dataSource.getConnection().getMetaData().getDriverName();
        return sqlType;
    }

    protected boolean isSqlType(String driverName) throws SQLException
    {
        return getSqlType().toUpperCase().contains(driverName);
    }

    protected String dataSourceName;
    protected DataSource dataSource;
    protected static Log logger = LogFactory.getLog(NativeRepository.class);
    private static SQLReader sqlReader;

    static {
        if (sqlReader == null) {
            synchronized (SQLReader.class) {
                if (sqlReader == null) {
                    sqlReader = new SQLReader();
                }
            }
        }
    }

    public NativeRepository()
    {
        Class<?> clazz = this.getClass();
        DataRepository annotation = clazz.getAnnotation(DataRepository.class);
        dataSourceName = annotation.name();
    }

}
