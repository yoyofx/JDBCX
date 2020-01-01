package com.bitauto.ep.fx.jdbcx;

import com.bitauto.ep.fx.jdbcx.ProcedureParam.ColumnType;
import com.bitauto.ep.fx.jdbcx.ProcedureParam.NoConverColumn;
//import com.bitauto.ep.fx.utils.common.DateUtil;
//import com.bitauto.ep.fx.utils.common.StringUtil;
//import com.bitauto.ep.fx.utils.reflect.ReflectUtil;
import com.bitauto.ep.fx.jdbcx.common.DateUtil;
import com.bitauto.ep.fx.jdbcx.common.ReflectUtil;
import com.bitauto.ep.fx.jdbcx.common.StringUtil;
import com.microsoft.sqlserver.jdbc.SQLServerDataColumn;
import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.sql.Types;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ProjectName: com.bitauto.ep.fx
 * @Package: com.bitauto.ep.fx.repositorys.BaseRepository
 * @ClassName: ConvertSqlDataTable
 * @Description:
 * @Author:
 * @CreateDate: 2018/5/3 11:36
 */
public class ConvertSqlDataTable
{
    private static Log logger = LogFactory.getLog(ConvertSqlDataTable.class);

    /**
     * 将集合转换为SQLServerDataTable
     *
     * @param paramList 待转换的集合对象
     * @param <T>       泛型
     * @return SQLServerDataTable
     */
    public static <T> SQLServerDataTable convertDataTable(List<T> paramList) throws SQLServerException, ParseException
    {
        if (paramList != null && paramList.size() > 0)
        {
            T entity = paramList.get(0);
            String className = entity.getClass().getName();

            if ( StringUtil.isNullOrEmpty(className))
            {
                logger.error("获取类名称失败");
                return null;
            }

            String[] columnName = ReflectUtil.getField(className, true);

            if (columnName == null || columnName.length <= 0)
            {
                logger.error("获取属性字段失败");
                return null;
            }

            SQLServerDataTable dt = createSourceTable(entity, className, columnName);

            Map<Integer, SQLServerDataColumn> columnMap = dt.getColumnMetadata();

            for (T param : paramList)
            {
                Object[] rowValue = new Object[columnMap.size()];
                setSqlDataTableRow(rowValue, param, columnMap);
                dt.addRow(rowValue);
            }

            return dt;
        }
        return null;
    }

    /**
     * 设置SQLServerDataTable Row
     *
     * @param value     值集合
     * @param entity    实体
     * @param <T>
     * @param columnMap
     */
    private static <T> void setSqlDataTableRow(Object[] value, T entity, Map<Integer, SQLServerDataColumn> columnMap) throws ParseException
    {
        for (Integer i = 0; i < columnMap.size(); i++)
        {
            if (Types.TIMESTAMP == columnMap.get(i).getColumnType())
            {
                Date date = (Date) ReflectUtil.getFieldValue(entity, columnMap.get(i).getColumnName());
                value[i] = dateToTime(date);
            }
            else
            {
                value[i] = ReflectUtil.getFieldValue(entity, columnMap.get(i).getColumnName());
            }
        }
    }


    /**
     * 创建SQLServerDataTable列
     *
     * @param entity
     * @param className  类名称
     * @param columnName 列集合
     * @param <T>
     * @return SQLServerDataTable
     * @throws SQLServerException
     */
    private static <T> SQLServerDataTable createSourceTable(T entity, String className, String[] columnName) throws SQLServerException
    {
        SQLServerDataTable serverTable = new SQLServerDataTable();
        Class theClass = ReflectUtil.loadClass(className);


        for (String s : columnName)
        {
            Annotation[] annotations = ReflectUtil.findFieldAnnotation(theClass, s);

            //存在注解
            if (annotations != null && annotations.length > 0)
            {
                int tuvalu = getAnnotationValue(annotations);
                if (tuvalu != -999)
                {
                    serverTable.addColumnMetadata(s, tuvalu);
                }
            }
            else
            {
                //通过值推断类型
                Object value = ReflectUtil.getFieldValue(entity, s);
                serverTable.addColumnMetadata(s, matchType(value));
            }
        }

        return serverTable;
    }

    /**
     * 获取注解设置的type值
     *
     * @param annotations
     * @return java.sql.Types类型值 ,匹配不上默认NVARCHAR
     */
    private static int getAnnotationValue(Annotation[] annotations)
    {
        int typeValue = 0;

        for (Annotation s : annotations)
        {
            if (s.annotationType().equals(NoConverColumn.class))
            {
                return -999;
            }
            else if (s.annotationType().equals(ColumnType.class))
            {
                typeValue = ((ColumnType) s).value();
                return typeValue;
            }
        }

        if (typeValue == 0)
        {
            logger.debug("未获取到指定注解，设置默认值NVARCHAR");
            typeValue = Types.NVARCHAR;
        }

        return typeValue;
    }


    /**
     * 通过值类型匹配java.sql.Types类型
     *
     * @param value 值
     * @return java.sql.Types类型值 ,匹配不上默认NVARCHAR
     */
    private static int matchType(Object value)
    {
        if (value == null)
        {
            return Types.NVARCHAR;
        }

        int typeValue = 0;
        switch (value.getClass().getTypeName())
        {
            case "java.lang.Integer":
            case "int":
                typeValue = Types.INTEGER;
                break;

            case "java.lang.Double":
            case "double":
                typeValue = Types.DOUBLE;
                break;

            case "java.lang.Float":
            case "float":
                typeValue = Types.FLOAT;
                break;


            case "java.lang.Long":
            case "long":
                typeValue = Types.BIGINT;
                break;

            case "java.lang.Short":
            case "short":
                typeValue = Types.SMALLINT;
                break;

            case "java.lang.Byte":
            case "byte":
                typeValue = Types.BINARY;
                break;

            case "java.lang.Boolean":
            case "boolean":
                typeValue = Types.BOOLEAN;
                break;
            case "java.lang.Character":
            case "char":
                typeValue = Types.CHAR;
                break;

            case "java.lang.String":
                typeValue = Types.NVARCHAR;
                break;
            default:
                logger.debug(MessageFormat.format("1未匹配类型，设置默认值为 NVARCHAR,TypeValue:{0}", value.getClass().getTypeName()));
                typeValue = Types.NVARCHAR;
                break;
        }
        return typeValue;
    }


    /**
     * 将java.util.Date对象转化为String字符串
     *
     * @param date 要格式的java.util.Date对象
     * @return 表示日期的字符串
     */
    private static java.sql.Timestamp dateToTime(Date date) throws ParseException
    {
        String strDate = DateUtil.dateTime(date);
        return DateUtil.strToSqlDate(strDate);
    }
}
