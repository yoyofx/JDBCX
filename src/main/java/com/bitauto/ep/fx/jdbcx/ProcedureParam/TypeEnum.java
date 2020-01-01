package com.bitauto.ep.fx.jdbcx.ProcedureParam;

/**
 * @ProjectName: fx
 * @Package: com.bitauto.ep.fx.repositorys.BaseEntity
 * @ClassName: TypeEnum
 * @Description:传入、传出、输出的结果集
 * @Author:
 * @CreateDate: 2018/4/28 16:52
 */
public enum TypeEnum
{

    INPUT("input", 0), OUTPUT("output", 1), OUTTABLE("outtable", 2);

    private String name;
    private int index;

    public String getName()
    {
        return name;
    }


    public int getIndex()
    {
        return index;
    }

    private TypeEnum(String name, int index)
    {
        this.name = name;
        this.index = index;
    }


    public static String getName(int index)
    {
        for (TypeEnum c : TypeEnum.values())
        {
            if (c.getIndex() == index)
            {
                return c.name;
            }
        }
        return null;
    }


}