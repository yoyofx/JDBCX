package com.bitauto.ep.fx.jdbcx.BeanMapper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.springframework.jdbc.support.JdbcUtils;
import javax.persistence.Column;


/**
 * <p>
 * <code>BeanProcessor</code> matches column names to bean property names and converts
 * <code>ResultSet</code> columns into objects for those bean properties. Subclasses should override
 * the methods in the processing chain to customize behavior.
 * </p>
 *
 * <p>
 * This class is thread-safe.
 * </p>
 *
 * 基于apache-dbutils，针对动态bean生成,修改了大部分方法的实现
 * <p>
 *
 */
class BeanProcessor {

    /**
     * 缓存动态生成的 DbBeanFactory，这里的cache是静态的，意味着无论BeanProcessor被new多少次，cache都是同一个
     */
    private static final Map<String, DbBeanFactory> static_cache = new ConcurrentHashMap<String, DbBeanFactory>();

    private Map<String, DbBeanFactory> realCache;

    /**
     * Special array value used by <code>mapColumnsToProperties</code> that indicates there is no
     * bean property that matches a column from a <code>ResultSet</code>.
     */
    private static final int PROPERTY_NOT_FOUND = -1;

    private static final int PROPERTY_TYPE_OTHER = 0;
    private static final int PROPERTY_TYPE_BOOLEAN = 1;
    private static final int PROPERTY_TYPE_BYTE = 2;
    private static final int PROPERTY_TYPE_SHORT = 3;
    private static final int PROPERTY_TYPE_INTEGER = 4;
    private static final int PROPERTY_TYPE_LONG = 5;
    private static final int PROPERTY_TYPE_FLOAT = 6;
    private static final int PROPERTY_TYPE_DOUBLE = 7;
    private static final int PROPERTY_TYPE_DATE = 8;
    private static final int PROPERTY_TYPE_TIMESTAMP = 9;

    private static final String DYNAMIC_BEAN_PACKAGE = "com.nway.spring.jdbc.bean.";

    /**
     * Set a bean's primitive properties to these defaults when SQL NULL is returned. These are the
     * same as the defaults that ResultSet get* methods return in the event of a NULL column.
     */
    private static final Map<Class<?>, Object> primitiveDefaults = new HashMap<Class<?>, Object>();

    static {

        primitiveDefaults.put(Integer.TYPE, 0);
        primitiveDefaults.put(Short.TYPE, (short) 0);
        primitiveDefaults.put(Byte.TYPE, (byte) 0);
        primitiveDefaults.put(Float.TYPE, 0.0f);
        primitiveDefaults.put(Double.TYPE, 0.0d);
        primitiveDefaults.put(Long.TYPE, 0L);
        primitiveDefaults.put(Boolean.TYPE, Boolean.FALSE);
        primitiveDefaults.put(Character.TYPE, (char) 0);

    }

    public BeanProcessor() {
        this(static_cache);
    }

    /**
     * Constructor for BeanProcessor configured with column to property name overrides.
     *
     * @param customCache
     * @since 1.5
     */
    public BeanProcessor(Map<String, DbBeanFactory> customCache) {
        this.realCache = customCache;
    }

    /**
     * Convert a <code>ResultSet</code> into a <code>List</code> of JavaBeans. This implementation
     * uses reflection and <code>BeanInfo</code> classes to match column names to bean property
     * names. Properties are matched to columns based on several factors: <br/>
     * <ol>
     * <li>
     * The class has a writable property with the same name as a column. The name comparison is case
     * insensitive.</li>
     *
     * <li>
     * The column type can be converted to the property's set method parameter type with a
     * ResultSet.get* method. If the conversion fails (ie. the property was an int and the column
     * was a Timestamp) an SQLException is thrown.</li>
     * </ol>
     *
     * <p>
     * Primitive bean properties are set to their defaults when SQL NULL is returned from the
     * <code>ResultSet</code>. Numeric fields are set to 0 and booleans are set to false. Object
     * bean properties are set to <code>null</code> when SQL NULL is returned. This is the same
     * behavior as the <code>ResultSet</code> get* methods.
     * </p>
     *
     * @param <T> The type of bean to create
     * @param rs ResultSet that supplies the bean data
     * @param type Class from which to create the bean instance
     * @throws SQLException if a database access error occurs
     * @return the newly created List of beans
     */
    public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException, NoSuchFieldException
    {

        if (!rs.next()) {
            return Collections.emptyList();
        }

        final List<T> results = new ArrayList<T>();

        do {
            results.add(toBean(rs, type));
        } while (rs.next());

        return results;
    }

    /**
     * 生成动态bean，并将数据和bean合并
     * @param <T>
     * @param rs {@link ResultSet}
     * @param type bean类型
     * @return 包含数据的bean
     * @throws SQLException
     */
    public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException, NoSuchFieldException
    {
        return createBeanByJDK(rs, type);
    }

    private <T> T createBeanByJDK(ResultSet rs, Class<T> type) throws SQLException, NoSuchFieldException
    {

        T bean = this.newInstance(type);

        ResultSetMetaData rsmd = rs.getMetaData();

        Field[] props = this.propertyDescriptors(type);

        int[] columnToProperty = this.mapColumnsToProperties(rsmd, props,type);

        for (int i = 1; i < columnToProperty.length; i++) {

            if (columnToProperty[i] == PROPERTY_NOT_FOUND) {
                continue;
            }

            Field prop = props[columnToProperty[i]];
            Class<?> propType = prop.getType();

            Object value = JdbcUtils.getResultSetValue(rs, i, propType);

            if (propType != null && value == null && propType.isPrimitive()) {

                // 无法获取对象中定义的默认值时，使用JDK规定的默认值
                value = primitiveDefaults.get(propType);
            }

            this.callSetter(bean, prop, value);
        }

        return bean;
    }


    /**
     * Calls the setter method on the target object for the given property. If no setter method
     * exists for the property, this method does nothing.
     *
     * @param target The object to set the property on.
     * @param prop The property to set.
     * @param value The value to pass into the setter.
     * @throws SQLException if an error occurs setting the property.
     */
    private void callSetter(Object target, Field prop, Object value)
            throws SQLException {

        prop.setAccessible(true);
//        Method setter = prop.getWriteMethod();
//
//        if (setter == null) {
//            return;
//        }

        try {
            prop.set(target,value);
//            // Don't call setter if the value object isn't the right type
//            // if (this.isCompatibleType(value, params[0])) {
//            setter.invoke(target, new Object[]{value});

        } catch (Exception e) {

            throw new SQLException("Cannot set " + prop.getName() + ": " + e.toString());
        }
    }

    /**
     * Factory method that returns a new instance of the given Class. This is called at the start of
     * the bean creation process and may be overridden to provide custom behavior like returning a
     * cached bean instance.
     *
     * @param <T> The type of object to create
     * @param c The Class to create an object from.
     * @return A newly created object of the Class.
     * @throws SQLException if creation failed.
     */
    protected <T> T newInstance(Class<T> c) throws SQLException {

        try {

            return c.newInstance();
        } catch (InstantiationException e) {

            throw new SQLException("Cannot create " + c.getName() + ": " + e.toString());
        } catch (IllegalAccessException e) {

            throw new SQLException("Cannot create " + c.getName() + ": " + e.toString());
        }
    }

    /**
     * Returns a PropertyDescriptor[] for the given Class.
     *
     * @param c The Class to retrieve PropertyDescriptors for.
     * @return A PropertyDescriptor[] describing the Class.
     * @throws SQLException if introspection failed.
     */
    private Field[] propertyDescriptors(Class<?> c) throws SQLException {

//        // Introspector caches BeanInfo classes for better performance
//        BeanInfo beanInfo = null;
//
//        try {
//
//            beanInfo = Introspector.getBeanInfo(c);
//        } catch (IntrospectionException e) {
//
//            throw new SQLException("Bean introspection failed: " + e.toString());
//        }
//
//        return beanInfo.getPropertyDescriptors();
        return c.getDeclaredFields();
    }

    /**
     * The positions in the returned array represent column numbers. The values stored at each
     * position represent the index in the <code>PropertyDescriptor[]</code> for the bean property
     * that matches the column name. If no bean property was found for a column, the position is set
     * to <code>PROPERTY_NOT_FOUND</code>.
     *
     * @param rsmd The <code>ResultSetMetaData</code> containing column information.
     *
     * @param props The bean property descriptors.
     *
     * @throws SQLException if a database access error occurs
     *
     * @return An int[] with column index to property index mappings. The 0th element is meaningless
     * because JDBC column indexing starts at 1.
     */
    protected int[] mapColumnsToProperties(ResultSetMetaData rsmd, Field[] props,Class<?> classType)
            throws SQLException, NoSuchFieldException
    {

        int cols = rsmd.getColumnCount();
        int[] columnToProperty = new int[cols + 1];

        Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);

        for (int col = 1; col <= cols; col++) {

            String columnName = rsmd.getColumnLabel(col);

            for (int i = 0; i < props.length; i++) {
//                if(props[i].getName().equalsIgnoreCase("class"))
//                    continue;

//                Field field = classType.getDeclaredField(props[i].getName());
                Field field = props[i];
                Column columnAnnotation = field.getAnnotation(Column.class);

                if (columnAnnotation == null) {
                    if (columnName.equalsIgnoreCase(props[i].getName())) {
                        columnToProperty[col] = i;
                        break;
                    }
                } else if (columnName.equalsIgnoreCase(columnAnnotation.name())) {
                    columnToProperty[col] = i;
                    break;
                }

            }
        }

        return columnToProperty;
    }


}
