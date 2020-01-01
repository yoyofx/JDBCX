package com.bitauto.ep.fx.jdbcx;

import com.bitauto.ep.fx.jdbcx.BeanMapper.BeanHandler;
import com.bitauto.ep.fx.jdbcx.BeanMapper.BeanListHandler;
import com.bitauto.ep.fx.jdbcx.Helper.EntitySQLHelper;
import com.bitauto.ep.fx.jdbcx.Query.EntityQueryBuilder;
import com.bitauto.ep.fx.jdbcx.Query.PageBuilder;
import com.bitauto.ep.fx.jdbcx.Query.QueryBuilder;
import com.bitauto.ep.fx.jdbcx.Query.QueryMode;
//import com.bitauto.ep.fx.utils.reflect.ReflectUtil;
import com.bitauto.ep.fx.jdbcx.common.ReflectUtil;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.NullArgumentException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.KeyHolder;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * 实体仓储，用于执行单表CURD
 * @param <TEntity> 单表实体类型
 */
public class BeanRepository<TEntity> extends NativeRepository {
    private String tableName = null;
    private Class<TEntity> entityClass;
    private HashMap<String,FieldMap> tableFieldList = new LinkedHashMap<>();

    public BeanRepository(){
        if(tableName == null){
           this.init();
        }
    }

    @SuppressWarnings("unchecked")
    private  void init(){
        entityClass = (Class<TEntity>)
                ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        tableName = EntitySQLHelper.getTableName(entityClass);
        tableFieldList = getColumns(entityClass);
    }


    /**
     * 持久化实体对象(insert)
     * @param object 实体对象
     * @return 影响行数
     */
    public long save(TEntity object) {
        SimpleJdbcInsert simpleJdbcInsert=new SimpleJdbcInsert(this.getNormalDB());
        simpleJdbcInsert.withTableName( tableName );
        List<String> gfields = new LinkedList<>();
        Map<String, Object> map = null;
        try {
            map= PropertyUtils.describe(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            logger.error(e);
        }catch (NoSuchMethodException e) {
            logger.error(e);
        }
        if (map!=null) {
            Field[] fields = object.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Column column=fields[i].getAnnotation(Column.class);
                if (fields[i].getAnnotation(Transient.class)!=null ) {
                    map.remove(fields[i].getName());
                    continue;
                }
                else if( fields[i].getAnnotation(GeneratedValue.class)!=null){
                    String columnName = column.name();
                    if (column == null) {
                        columnName = fields[i].getName();
                    }
                    gfields.add(columnName);
                }
                Object value=map.get(fields[i].getName());
                if (column!=null) {
                    map.remove(fields[i].getName());
                    map.put(column.name(), value);
                }else {
                    continue;
                }
            }
            ;
            simpleJdbcInsert.usingGeneratedKeyColumns(gfields.toArray(new String[0]));
            //simpleJdbcInsert.compile();
            KeyHolder keyHolder = simpleJdbcInsert.executeAndReturnKeyHolder(map);
            return keyHolder.getKey().longValue();
            //return simpleJdbcInsert.execute(map);

        }else {
            return 0;
        }
    }

    /**
     * 更新持久对象(update)
     * @param object 实体对象
     * @return 影响行数
     */
    public int update(TEntity object){
        //UPDATE Person SET FirstName = 'Fred' WHERE LastName = 'Wilson'
        //ReflectUtil.getv
        StringBuilder sqlBuffer = new StringBuilder("UPDATE ");
        sqlBuffer.append(tableName).append(" SET");
        List<Object> paramList = new LinkedList<>();
        String idField = null;
        Object idValue = null;
        for (Map.Entry<String,FieldMap> entry :tableFieldList.entrySet() ) {
            FieldMap field = entry.getValue();
            Object fieldValue = ReflectUtil.getFieldValue(object , field.FieldName);
            if(field.IsId){
                idField = field.ColumnName;
                idValue = fieldValue;
                continue;
            }
            paramList.add(fieldValue);
            sqlBuffer.append(" ").append(field.ColumnName).append(" = ?,");
        }

        if(idField == null){
            throw new NullArgumentException("ID field not found!");
        }

        if(!tableFieldList.isEmpty()) {
            sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        }

        paramList.add(idValue);
        sqlBuffer.append(" WHERE ").append(idField).append(" = ?");

        return this.executeRawSql(sqlBuffer.toString(),paramList.toArray());
    }

    /**
     * 加载指定ID的实体
     * @param id 数据表中的ID字段的值
     * @return 指定ID的实体
     */
    public TEntity get( Object id ){
        return this.load(id);
    }

    /**
     * 加载指定ID的实体
     * @param id 数据表中的ID字段的值
     * @return 指定ID的实体
     */
    public TEntity load( Object id ) {
        try {
            String sql = getSelectSQL(entityClass);
            List<TEntity> list= this.getNormalDB().query(sql,new Object[]{id}, new BeanListHandler<>(entityClass));
            if (list.size()>0) {
                return list.get(0);
            }else {
                return null;
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 删除指定ID的实体
     * @param id 数据表ID
     * @return 影响行数
     */
    public int delete( Object id ){
        Class<TEntity> entityClass = (Class<TEntity>)
                ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Optional<FieldMap> idFieldMap = getTableIdFieldMap(entityClass);
        if(!idFieldMap.isPresent()){
            throw new InputMismatchException("id not found!");
        }
        FieldMap field = idFieldMap.get();
        StringBuffer sqlBuff = new StringBuffer("DELETE FROM ");
        sqlBuff.append(tableName)
                .append(" WHERE ").append(field.ColumnName).append("= ?");

        return this.executeRawSql(sqlBuff.toString(),new Object[]{ id });
    }

    public List<TEntity> queryForList(QueryBuilder queryBuilder){
        String sql = queryBuilder.Build();
        return  getNormalDB().query(sql,queryBuilder.getParameters().toArray(),new BeanListHandler<>(entityClass));
    }

    public  TEntity queryForEntity(QueryBuilder queryBuilder){
        String sql = queryBuilder.Build();
        return getNormalDB().query(sql,queryBuilder.getParameters().toArray(),new BeanHandler<>(entityClass));
    }

    private String getSelectSQL(Class<TEntity> clazz) {
        Optional<String> idfop = getTableId(clazz);
        if(!idfop.isPresent())
        {
            throw new InputMismatchException("id not found!");
        }
        String sql = "SELECT * FROM " + tableName +" WHERE " + idfop.get() +"= ? ";
        return sql;
    }



    private static HashMap<String,FieldMap> getColumns(Class<?> clazz){
        HashMap<String,FieldMap> columns = new LinkedHashMap<>();
        Field[] fields = clazz.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getAnnotation(Transient.class)!=null) {
                continue;
            }
            String columnName = null;
            String fieldName = fields[i].getName();
            Column column=fields[i].getAnnotation(Column.class);
            if (column!=null) {
                columnName = column.name();
            }else {
                columnName = fieldName;
            }
            FieldMap fieldMap = new FieldMap();
            fieldMap.FieldName = fieldName;
            fieldMap.ColumnName = columnName;

            if (fields[i].getAnnotation(Id.class)!=null) {
                fieldMap.IsId = true;
            }
            if (fields[i].getAnnotation(GeneratedValue.class)!=null){
                fieldMap.IsGenerated = true;
            }
            columns.put(columnName,fieldMap);
        }
        return  columns;
    }


    private Optional<String> getTableId(Class<?> clazz){
        Optional<String> id = tableFieldList.entrySet().stream().
                filter(f -> f.getValue().IsId).map(f->f.getValue().ColumnName).findFirst();
        return id;
    }


    private Optional<FieldMap> getTableIdFieldMap(Class<?> clazz){
        Optional<FieldMap> id = tableFieldList.entrySet().stream().
                filter(f -> f.getValue().IsId).map(f->f.getValue()).findFirst();
        return id;
    }

}
