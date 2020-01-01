package com.bitauto.ep.fx.jdbcx.Query;

import com.bitauto.ep.fx.jdbcx.ICallableStatementInput;
import com.bitauto.ep.fx.jdbcx.IPaginationSupport;
import com.bitauto.ep.fx.jdbcx.Pageination.MSSqlPaginationSupport;
import com.bitauto.ep.fx.jdbcx.Pageination.MySqlPaginationSupport;
import com.bitauto.ep.fx.jdbcx.Provider.SqlServerCallableStatementInput;
import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;
import com.microsoft.sqlserver.jdbc.SQLServerDataTable;

import java.sql.CallableStatement;
import java.sql.SQLException;

public abstract class ObjectAbstractFactory {

    private static final MSSqlPaginationSupport msSqlPaginationSupport = new MSSqlPaginationSupport();
    private static final MySqlPaginationSupport mySqlPaginationSupport = new MySqlPaginationSupport();

    public static void addInputParameter(String sqlType, CallableStatement cstmt, Object dataValue, int parameterIndex, String paramName) throws SQLException {
        if (isSqlType(sqlType, "SQL SERVER")) {
            ICallableStatementInput callableStatementInput = new SqlServerCallableStatementInput();
            if (callableStatementInput.checkValueType(dataValue)) {
                callableStatementInput.addInputParameter(cstmt, dataValue, parameterIndex, paramName);
            }
        }else{
            //no match any types by set default value
            cstmt.setObject(parameterIndex,dataValue);
        }
    }


    public static IPaginationSupport getPaginationSupport(String sqlType) {
        if (isSqlType(sqlType, "SQL SERVER")) {
            return msSqlPaginationSupport;
        }
        if (isSqlType(sqlType, "MYSQL")) {
            return mySqlPaginationSupport;
        }
        return null;
    }

    private static boolean isSqlType(String sqlType, String driverName) {
        return sqlType.toUpperCase().contains(driverName);
    }
}
