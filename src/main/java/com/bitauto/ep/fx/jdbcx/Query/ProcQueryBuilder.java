package com.bitauto.ep.fx.jdbcx.Query;

import com.bitauto.ep.fx.jdbcx.ProcedureParam.ParamEntity;
import com.bitauto.ep.fx.jdbcx.ProcedureParam.TypeEnum;

import java.util.LinkedList;
import java.util.List;

public class ProcQueryBuilder
{
    private List<ParamEntity> parameterList = new LinkedList<>();
    private String procName;

    public ProcQueryBuilder(String procame){
        this.procName = procame;
    }

    public void AddParam(ParamEntity param){
        parameterList.add(param);
    }

    public void AddInputParam(String name,Object value){
        ParamEntity paramEntity =  new ParamEntity(name, value, TypeEnum.INPUT);
        parameterList.add(paramEntity);
    }

    public void AddOutputParam(String name,Object value){
        ParamEntity paramEntity =  new ParamEntity(name, value, TypeEnum.OUTPUT);
        parameterList.add(paramEntity);
    }



    public String getProcName()
    {
        return procName;
    }

    public List<ParamEntity> getParameters(){
        return parameterList;
    }
}
