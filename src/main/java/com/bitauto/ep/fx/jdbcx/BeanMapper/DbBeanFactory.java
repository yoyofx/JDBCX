package com.bitauto.ep.fx.jdbcx.BeanMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DbBeanFactory {

    protected abstract <T> T createBean(ResultSet rs, Class<T> type) throws SQLException;
}
