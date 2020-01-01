package com.bitauto.ep.fx.jdbcx.BeanMapper;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;


/**
 * <code>ResultSetHandler</code> implementation that converts the first <code>ResultSet</code> row
 * into a JavaBean. This class is thread safe.
 *
 * @param <T> the target bean type
 */
public class BeanHandler<T> implements ResultSetExtractor<T> {

    /**
     * The Class of beans produced by this handler.
     */
    private final Class<T> type;

    /**
     * 考虑本类可能在不同 ClassLoader中使用，这里不应该是静态的 *
     */
    private final BeanProcessor beanProcessor;

    /**
     * Creates a new instance of BeanHandler.
     *
     * @param type The Class that objects returned from <code>handle()</code> are created from.
     */
    public BeanHandler(Class<T> type) {

        this.type = type;
        this.beanProcessor = new BeanProcessor();
    }

    public BeanHandler(Class<T> type, Map<String, DbBeanFactory> cache) {

        this.type = type;
        this.beanProcessor = new BeanProcessor(cache);
    }

    /**
     * Convert the first row of the <code>ResultSet</code> into a bean with the <code>Class</code>
     * given in the constructor.
     *
     * @param rs <code>ResultSet</code> to process.
     * @return An initialized JavaBean or <code>null</code> if there were no rows in the
     * <code>ResultSet</code>.
     *
     * @throws SQLException if a database access error occurs
     */
    @Override
    public T extractData(ResultSet rs) throws DataAccessException {

        try {
            return rs.next() ? beanProcessor.toBean(rs, this.type) : null;
        } catch (SQLException | NoSuchFieldException e) {
            throw new NoClassDefFoundError("创建动态对象失败 [ " + this.type + " ],Error:"+ e.getMessage());
        }
    }

}