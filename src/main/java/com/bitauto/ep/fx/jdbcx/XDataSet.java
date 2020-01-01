package com.bitauto.ep.fx.jdbcx;

import com.bitauto.ep.fx.jdbcx.Helper.MapEntityMapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class XDataSet
{
    //Map<String, List<Map<String,Object>>> dataset;
    private Map dataset;
    public XDataSet(Map data){
        this.dataset = data;
    }

    /**
     * 是否存在结果集
     * @return true or false
     */
    public boolean hasTable(){
        return dataset!=null && dataset.size() > 0;
    }

    /**
     * 获取结果集数量
     * @return
     */
    public int getTableCount(){
        return dataset.size();
    }

    /**
     * 获取指定索引结果集中的行数
     * @param tableIndex 结果集索引
     * @return
     */
    public int getTableRowCount(int tableIndex){
        if(hasTable()){
            List table = getTable(tableIndex);
            if(table!=null)
                return table.size();
        }
        return 0;
    }




    /**
     * 获取第一个结果集中的第一行第一列的值
     * @return Object
     */
    public Object getSingleValue(){
        Object value = null;
        Object[] values = getSingleValues();
        if(values!=null && values.length > 0){
            value = values[0];
        }
        return value;
    }

    /**
     * 获取第一个结果集中的第一行的所有值
     * @return Object[]
     */
    public Object[] getSingleValues(){
        Map row = getRow(0,0);
        return row.values().toArray();
    }


    /**
     * 获取指定索引下结果集中的实体列表
     * @param tableIndex 结果集索引
     * @param classz 返回实体class
     * @param <QEntity> 实体类型
     * @return
     */
    public <QEntity> List<QEntity> getResultForList(int tableIndex,Class<QEntity> classz){
        ArrayList<QEntity> resultList = new ArrayList<>();
        List table = getTable(tableIndex);
        for (Object rowMap: table ){
            Map<String,Object> row =  (Map<String,Object>)rowMap;
            QEntity rowItem =  MapEntityMapper.mapToObject(row,classz);
            if(rowItem!=null){
                resultList.add(rowItem);
            }
        }
        if(resultList.size() > 0) return resultList;
        return null;
    }

    /**
     * 获取第一个结果集中的实体列表
     * @param classz 返回实体class
     * @param <QEntity> 实体类型
     * @return
     */
    public <QEntity> List<QEntity> getFirstResultForList(Class<QEntity> classz)
    {
        return getResultForList(0,classz);
    }

    /**
     * 获取指定索引结果集中的实体
     * @param tableIndex 结果集索引
     * @param rowIndex 结果集中的行索引
     * @param classz 实体class
     * @param <QEntity> 实体类型
     * @return
     */
    public <QEntity> QEntity getResultByIndex(int tableIndex,int rowIndex,Class<QEntity> classz)
    {
        QEntity result = null;
        Map<String,Object> row = (Map<String,Object>) getRow(tableIndex,rowIndex);
        result =  MapEntityMapper.mapToObject(row,classz);
        return result;
    }




    /**
     * 获取第tableIndex个结果集
     * @param tableIndex 结果集索引
     * @return List<Map<String,Object>>
     */
    public List getTable(int tableIndex){
        List dataTable = null;
        if(dataset.size()>=tableIndex ) {
            dataTable = (List) dataset.get(String.valueOf(tableIndex));
        }
        return dataTable;
    }

    /**
     * 获取第tableIndex个结果集中的第rowIndex行的记录
     * @param tableIndex 结果集索引
     * @param rowIndex 行索引
     * @return Map<String,Object>
     */
    public Map getRow(int tableIndex,int rowIndex){
        Map row = null;
        List dataTable = getTable(tableIndex);
        if(dataTable!=null && dataTable.size() >= rowIndex) {
            row = (Map) dataTable.get(rowIndex);
        }
        return row;
    }

    /**
     * 获取结果集中的值，一般情况下与存储过程中的out参数对应
     * @param key out参数名
     * @return Object
     */
    public Object getOutputValue(String key){
        return dataset.get(key);
    }


}
