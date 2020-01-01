package com.bitauto.ep.fx.jdbcx.BeanMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;


/**
 *
 * @param <T> List元素类型
 */
public final class BeanListHandler<T> implements ResultSetExtractor<List<T>> {

    private final Class<T> type;

    /**
     * 考虑本类可能在不同 ClassLoader中使用，这里不应该是静态的 *
     */
    private final BeanProcessor beanProcessor;

    /**
     *
     *
     * @param type
     */
    public BeanListHandler(Class<T> type) {

        this.type = type;
        this.beanProcessor = new BeanProcessor();
    }

    public BeanListHandler(Class<T> type, Map<String, DbBeanFactory> cache) {

        this.type = type;
        this.beanProcessor = new BeanProcessor(cache);
    }

    @Override
    public List<T> extractData(ResultSet rs) throws DataAccessException {

        try {
            return beanProcessor.toBeanList(rs, type);
        } catch (SQLException | NoSuchFieldException e) {
            throw new NoClassDefFoundError("获取列表失败 [ " + this.type + " ], ERROR:"+e.getMessage());
        }
    }

}
