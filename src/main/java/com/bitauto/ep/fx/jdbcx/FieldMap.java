package com.bitauto.ep.fx.jdbcx;

public class FieldMap{
    public FieldMap(){
        this.IsId = false;
        this.IsGenerated = false;
    }


    public String ColumnName;
    public String FieldName;
    public Boolean IsId;
    public Boolean IsGenerated;
}