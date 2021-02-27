package com.jaiz.web.gen.service;

import com.jaiz.web.gen.eneity.ColumnsVO;
import com.jaiz.web.gen.eneity.MapperNodesVO;
import com.jaiz.web.gen.eneity.PojoPropertiesVO;
import com.jaiz.web.gen.eneity.TablesVO;
import com.jaiz.web.gen.mapper.SchemaMapper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Service
public class IndexService {

    @Autowired
    private SchemaMapper schemaMapper;

    @Autowired
    @Qualifier("schemaName")
    private String schema;

    /**
     * 查看所有表
     * @return
     */
    public List<TablesVO> showIndex(String name) {
        return schemaMapper.selectTablesBySchemaName(schema,name);
    }

    /**
     * 查询表的所有字段，封装为java类属性对象
     * @param name
     * @return
     */
    public List<PojoPropertiesVO> selectPojoProperties(String name) {
        List<ColumnsVO> columns=schemaMapper.selectColumnsByTableName(schema,name);
        return columns.stream().map(this::column2Property).collect(Collectors.toList());
    }

    /**
     * 列转属性
     * @param column
     * @return
     */
    PojoPropertiesVO column2Property(ColumnsVO column){
        PojoPropertiesVO prop=new PojoPropertiesVO();
        prop.setPropertyName(dash2Camel(column.getColumnName()));
        prop.setPropertyType(mysqlType2JavaType(column.getDataType()));
        prop.setPropertyDesc(column.getColumnComment());
        return prop;
    }

    /**
     * mysql数据库数据类型转java类型
     * @param dataType
     * @return
     */
    private String mysqlType2JavaType(String dataType) {
        switch (dataType){
            case "bigint":
                return "Long";
            case "varchar":
                return "String";
            case "int":
                return "Integer";
            case "tinyint":
                return "Integer/Boolean";
            case "double":
                return "Double";
            case "datetime":
            case "timestamp":
                return "Date";
            default:
                throw new RuntimeException("不识别的类型，请补充至源代码");
        }
    }

    /**
     * 数据库下滑线命名转java字段命名
     * @param columnName
     * @return
     */
    String dash2Camel(String columnName) {
        String lowerColumnName=columnName.toLowerCase();
        String[] propertyNameParts=lowerColumnName.split("_");
        if (propertyNameParts.length==1){
            return propertyNameParts[0];
        }
        StringBuilder sb=new StringBuilder(propertyNameParts[0]);
        for (int i=1;i<propertyNameParts.length;i++){
            String word=propertyNameParts[i];
            char firstCharUpper=Character.toUpperCase(word.charAt(0));
            //if(Character.isLetter(firstChar)){
            //
            //}
            sb.append(firstCharUpper);
            if (word.length()>1){
                sb.append(word.substring(1));
            }
        }
        return sb.toString();
    }

    /**
     *
     * @param name
     * @return
     */
    public MapperNodesVO selectMapperNodes(String name) {
        List<ColumnsVO> columns=schemaMapper.selectColumnsByTableName(schema,name);
        var columnNames=columns.stream().map(ColumnsVO::getColumnName).collect(Collectors.toList());
        var resultMapCpPairs=columns.stream().filter(c->{
            //不要id
            return !c.getColumnName().equals("ID");
        }).map(this::column2CpPair).collect(Collectors.toList());
        var updateInsertCpPairs=columns.stream().filter(c->{
            //不要id，updateTime,createTime
            return !c.getColumnName().equals("ID") && !c.getColumnName().equals("UPDATE_TIME") && !c.getColumnName().equals("CREATE_TIME");
        }).map(this::column2CpPair).collect(Collectors.toList());
        var vo=new MapperNodesVO();
        vo.setColumnNames(columnNames);
        vo.setResultMapCpPairs(resultMapCpPairs);
        vo.setUpdateInsertCpPairs(updateInsertCpPairs);
        return vo;
    }

    private MutablePair<String,String> column2CpPair(ColumnsVO c){
        return new MutablePair<>(c.getColumnName(),dash2Camel(c.getColumnName()));
    }

    public List<MutablePair<String,String>> selectInsertColumnsContent(String name) {
        var columns=schemaMapper.selectColumnsByTableName(schema,name);
        return columns.stream().filter(c->{
           var cn= c.getColumnName();
           return !cn.equals("ID") && !cn.equals("CREATE_TIME") && !cn.equals("UPDATE_TIME");
        }).map(c-> new MutablePair<>(c.getColumnName(),dash2Camel(c.getColumnName()))).collect(Collectors.toList());
    }
}
