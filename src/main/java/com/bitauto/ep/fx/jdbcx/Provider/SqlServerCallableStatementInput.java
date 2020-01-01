package com.bitauto.ep.fx.jdbcx.Provider;

import com.bitauto.ep.fx.jdbcx.ICallableStatementInput;
import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;
import com.microsoft.sqlserver.jdbc.SQLServerDataTable;

import java.sql.CallableStatement;
import java.sql.SQLException;

public class SqlServerCallableStatementInput implements ICallableStatementInput
{
    @Override
    public boolean checkValueType(Object dataValue)
    {
        return dataValue instanceof SQLServerDataTable ;
    }

    @Override
    public void addInputParameter(CallableStatement cstmt, Object dataValue, int parameterIndex, String paramName) throws SQLException
    {
        if (dataValue instanceof SQLServerDataTable )
        {
            SQLServerCallableStatement sqlServerCallableStatement = cstmt.unwrap(SQLServerCallableStatement.class);
            sqlServerCallableStatement.setStructured(parameterIndex, paramName, (SQLServerDataTable) dataValue);
        }
    }
}
