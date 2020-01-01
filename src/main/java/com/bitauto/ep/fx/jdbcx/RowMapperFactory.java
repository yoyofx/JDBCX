package com.bitauto.ep.fx.jdbcx;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RowMapperFactory {

    public static <K> RowMapper<K> getInstance(Class<K> clazz) {
        return getRowMapper(clazz);
    }

    public static <K> RowMapper<K> getRowMapper(Class<K> clazz) {

        return new RowMapper<K>() {

            private AutoRowMapper<K> rm = new AutoRowMapper(clazz);

            @Override
            public K mapRow(ResultSet rs, int rowNum) throws SQLException {
                try {
                    return rm.mapRow(rs, rowNum, clazz.newInstance());
                }
                catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    /**
     * 这是一个用于简化 rowMapper 的编写辅助类。
     * <p/>
     * 本类会自动跳过 entity 中存在，但是在查询语句中不存在的列。
     *
     * @param <T> entity type
     */
    private static class AutoRowMapper<T> {

        private Map<String, String> colName2SetterMap = new HashMap<>(); // 数据列名到 setter 名称的映射
        private Map<String, Method> setterMap = new HashMap<>();         // setter 名称到 setter 方法的映射
        private Map<String, String> setterParamMap = new HashMap<>();    // setter 名称到 setter 参数类型的映射

        /**
         * 初始化
         *
         * @param t 实体类的类对象
         */
        protected <T> AutoRowMapper(Class<T> t) {
            Method[] ms = t.getMethods();
            for (int i = 0; i < ms.length; i++) {
                String name = ms[i].getName();
                if (name.startsWith("set") && ms[i].getParameterCount() == 1) {
                    setterMap.put(name, ms[i]);
                    setterParamMap.put(name, ms[i].getGenericParameterTypes()[0].getTypeName());
                    colName2SetterMap.put(setterToColName(name), name);
                }
            }
        }

        /**
         * 在子类中，要实现的 RowMapper.mapRow 中掉用此方法。
         *
         * @param rs     结果集
         * @param rowNum 结果集行号
         * @param t      entity 对象，用于装填查询结果行数据
         * @return 传入的 entity 对象
         */
        public T mapRow(ResultSet rs, int rowNum, T t) {

            for (String col : colName2SetterMap.keySet()) {
                try {
                    int index = rs.findColumn(col); // 如果找不到列，就会抛出异常
                    String setterName = colName2SetterMap.get(col);
                    inject(setterMap.get(setterName), setterParamMap.get(setterName), rs, index, t);
                } catch (SQLException ex) {
                    continue;
                }
            }
            return t;
        }

        /**
         * 把 setter 名称转换为列名。如 setCreatedOn --> created_on
         */
        private static String setterToColName(String setterName) {
            String property = setterName.substring(3, setterName.length());
//            StringBuilder sb = new StringBuilder().append(property.charAt(0));
//            for(int i = 1; i < property.length(); i++) {
//                char c = property.charAt(i);
//                if(Character.isUpperCase(c)) {
//                    sb.append('_');
//                }
//                sb.append(c);
//            }
            return property;
        }

        /**
         * 把指定列按类型注入 entity.
         * <p/>
         * 目前支持的类字段类型有：
         * <pre>
         * java.lang.Boolean    boolean
         * java.lang.Byte       byte
         * java.lang.Long       long
         * java.lang.Integer    int
         * java.lang.Short      short
         * java.lang.Float      float
         * java.lang.Double     double
         * java.lang.Date
         * java.lang.String
         * java.sql.Blob
         * java.math.BigDecimal
         * </pre>
         */
        private void inject(Method getter, String fieldType, ResultSet rs, int index, T t) {
            try {

                switch (fieldType) {
                    case "java.lang.Boolean":  // 布尔值
                    case "boolean":
                        getter.invoke(t, rs.getBoolean(index));
                        break;
                    case "java.lang.Byte":     // 字节
                    case "byte":
                        getter.invoke(t, rs.getByte(index));
                        break;
                    case "java.lang.Long":     // Long
                    case "long":
                        getter.invoke(t, rs.getLong(index));
                        break;
                    case "java.lang.Integer":  // Int
                    case "int":
                        getter.invoke(t, rs.getInt(index));
                        break;
                    case "java.lang.Short":    // Short
                    case "short":
                        getter.invoke(t, rs.getShort(index));
                        break;
                    case "java.lang.Float":    // Float
                    case "float":
                        getter.invoke(t, rs.getFloat(index));
                        break;
                    case "java.lang.Double":   // Double
                    case "double":
                        getter.invoke(t, rs.getDouble(index));
                        break;
                    case "java.lang.Date":
                        getter.invoke(t, rs.getDate(index));
                        break;
                    case "java.lang.String":
                        getter.invoke(t, rs.getString(index));
                        break;
                    case "java.sql.Blob":
                        getter.invoke(t, rs.getBlob(index));
                        break;
                    case "java.math.BigDecimal":
                        getter.invoke(t, rs.getBigDecimal(index));
                        break;
                    default:
                        getter.invoke(t, rs.getObject(index));
                        break;
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
