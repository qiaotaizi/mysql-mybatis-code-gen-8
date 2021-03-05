package com.jaiz.web.gen.service;

import com.jaiz.web.gen.eneity.*;
import com.jaiz.web.gen.mapper.SchemaMapper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
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
     * 根据字段类型给出随机值
     * @param propertyType
     * @return
     */
    private String randomValueInsert(String propertyType) {
        return switch (propertyType) {
            case "Long" -> "123L";
            case "String" -> "\"RandomString\"";
            case "Integer" -> "123";
            case "Integer/Boolean" -> "123/true";
            case "Double" -> "123.0";
            case "Date" -> "DateUtil.parse(\"2021-01-01 03:02:01\")";
            default -> throw new RuntimeException("不识别的类型，请补充至源代码");
        };
    }
    private String randomValueUpdate(String propertyType) {
        return switch (propertyType) {
            case "Long" -> "321L";
            case "String" -> "\"StringRandom\"";
            case "Integer" -> "321";
            case "Integer/Boolean" -> "321/false";
            case "Double" -> "321.2";
            case "Date" -> "DateUtil.parse(\"2021-02-02 01:02:03\")";
            default -> throw new RuntimeException("不识别的类型，请补充至源代码");
        };
    }

    /**
     * mysql数据库数据类型转java类型
     * @param dataType
     * @return
     */
    private String mysqlType2JavaType(String dataType) {
        return switch (dataType) {
            case "bigint" -> "Long";
            case "varchar" -> "String";
            case "int", "smallint","mediumint" -> "Integer";
            case "tinyint" -> "Integer/Boolean";
            case "double" -> "Double";
            case "datetime", "timestamp" -> "Date";
            default -> throw new RuntimeException("不识别的类型，请补充至源代码");
        };
    }

    /**
     * 数据库下滑线命名转java字段命名
     * @param columnName
     * @return
     */
    public String dash2Camel(String columnName) {
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

    public List<ColumnsVO> selectInsertColumnsContent(String name) {
        var columns=schemaMapper.selectColumnsByTableName(schema,name);
        return columns.stream().filter(c->{
           var cn= c.getColumnName();
           return !cn.equals("ID") && !cn.equals("CREATE_TIME") && !cn.equals("UPDATE_TIME");
        }).collect(Collectors.toList());
    }

    /**
     * 查询单元测试元信息
     * @param name
     */
    public UnitTestMetaVO selectUnitTestMeta(String name) {
        var insertColumns=selectInsertColumnsContent(name);
        UnitTestMetaVO vo=new UnitTestMetaVO();
        List<UnitTestMetaVO.InsertTestMeta> list=insertColumns.stream().map(c->{
            UnitTestMetaVO.InsertTestMeta meta=new UnitTestMetaVO.InsertTestMeta();
            meta.setDataType(mysqlType2JavaType(c.getDataType()));
            String varName=dash2Camel(c.getColumnName());
            meta.setVarName(varName);
            meta.setVarValue(randomValueInsert(meta.getDataType()));
            meta.setVarValueUpdated(randomValueUpdate(meta.getDataType()));
            String accessorSuffix=Character.toUpperCase(varName.charAt(0))+varName.substring(1);
            meta.setSetterName("set"+accessorSuffix);
            meta.setGetterName("get"+accessorSuffix);
            return meta;
        }).collect(Collectors.toList());
        vo.setInsertTestMetaList(list);
        return vo;
    }
}
