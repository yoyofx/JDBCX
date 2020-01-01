package com.bitauto.ep.fx.jdbcx.ProcedureParam;

import java.util.Objects;

/**
 * @ProjectName: fx
 * @Package: com.bitauto.ep.fx.repositorys.BaseEntity
 * @ClassName: ParamEntity
 * @Description:
 * @Author:
 * @CreateDate: 2018/4/28 16:52
 */
public class ParamEntity
{
    /**
     * 字段名称
     */
    private String name;

    private Object value;

    private TypeEnum type;

    private String dataType;


    /**
     * 构建参数实体
     * @param name 字段名称
     * @param value 字段值
     * @param type 参数类型
     */
    public ParamEntity(String name, Object value, TypeEnum type)
    {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    /**
     * 构建参数实体 参数类型默认 INPUT
     * @param name 字段名称
     * @param value 字段值
     */
    public ParamEntity(String name, Object value)
    {
        this.name = name;
        this.value = value;
        this.type = TypeEnum.INPUT;
    }


    public ParamEntity()
    {

    }


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public TypeEnum getType()
    {
        return type;
    }

    public void setType(TypeEnum type)
    {
        this.type = type;
    }

    public String getDataType()
    {
        return dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ParamEntity that = (ParamEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value) && type == that.type && Objects.equals(dataType, that.dataType);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, value, type, dataType);
    }
}

