package com.bitauto.ep.fx.jdbcx;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface ICallableStatementInput
{
    boolean checkValueType(Object dataValue);

    void addInputParameter(CallableStatement cstmt, Object dataValue, int parameterIndex, String paramName) throws SQLException;
}
