package com.jaiz.web.gen.service;

import com.jaiz.web.gen.eneity.*;
import com.jaiz.web.gen.mapper.SchemaMapper;
import com.jaiz.web.gen.utils.NameUtil;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.template.TemplateAvailabilityProvider;
import org.springframework.stereotype.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IndexService {

    @Autowired
    private SchemaMapper schemaMapper;

    @Autowired
    @Qualifier("schemaName")
    private String schema;

    @Value("${gen.base.dir}")
    private String tmpDir;

    /**
     * 查看所有表
     *
     * @return
     */
    public List<TablesVO> showIndex(String name) {
        return schemaMapper.selectTablesBySchemaName(schema, name);
    }

    /**
     * 查询表的所有字段，封装为java类属性对象
     *
     * @param name
     * @return
     */
    public List<PojoPropertiesVO> selectPojoProperties(String name) {
        List<ColumnsVO> columns = schemaMapper.selectColumnsByTableName(schema, name);
        return columns.stream().map(this::column2Property).collect(Collectors.toList());
    }

    /**
     * 列转属性
     *
     * @param column
     * @return
     */
    PojoPropertiesVO column2Property(ColumnsVO column) {
        PojoPropertiesVO prop = new PojoPropertiesVO();
        prop.setPropertyName(NameUtil.dashName2Camel(column.getColumnName()));
        prop.setPropertyType(mysqlType2JavaType(column.getDataType()));
        prop.setPropertyDesc(column.getColumnComment());
        return prop;
    }

    /**
     * 根据字段类型给出随机值
     *
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
     *
     * @param dataType
     * @return
     */
    private String mysqlType2JavaType(String dataType) {
        return switch (dataType) {
            case "bigint" -> "Long";
            case "varchar","text" -> "String";
            case "int", "smallint", "mediumint" -> "Integer";
            case "tinyint" -> "Integer/Boolean";
            case "double" -> "Double";
            case "datetime", "timestamp" -> "Date";
            default -> throw new RuntimeException("不识别的类型，请补充至源代码: "+dataType);
        };
    }

    /**
     * @param name
     * @return
     */
    public MapperNodesVO selectMapperNodes(String name) {
        List<ColumnsVO> columns = schemaMapper.selectColumnsByTableName(schema, name);
        var columnNames = columns.stream().map(ColumnsVO::getColumnName).collect(Collectors.toList());
        var resultMapCpPairs = columns.stream().filter(c -> {
            //不要id
            return !c.getColumnName().equals("ID");
        }).map(this::column2CpPair).collect(Collectors.toList());
        var updateInsertCpPairs = columns.stream().filter(c -> {
            //不要id，updateTime,createTime
            return !c.getColumnName().equals("ID") && !c.getColumnName().equals("UPDATE_TIME") && !c.getColumnName().equals("CREATE_TIME");
        }).map(this::column2CpPair).collect(Collectors.toList());
        var vo = new MapperNodesVO();
        vo.setColumnNames(columnNames);
        vo.setResultMapCpPairs(resultMapCpPairs);
        vo.setUpdateInsertCpPairs(updateInsertCpPairs);
        return vo;
    }

    private MutablePair<String, String> column2CpPair(ColumnsVO c) {
        return new MutablePair<>(c.getColumnName(), NameUtil.dashName2Camel(c.getColumnName()));
    }

    public List<ColumnsVO> selectInsertColumnsContent(String name) {
        var columns = schemaMapper.selectColumnsByTableName(schema, name);
        return columns.stream().filter(c -> {
            var cn = c.getColumnName();
            return !cn.equals("ID") && !cn.equals("CREATE_TIME") && !cn.equals("UPDATE_TIME");
        }).collect(Collectors.toList());
    }

    /**
     * 查询单元测试元信息
     *
     * @param name
     */
    public UnitTestMetaVO selectUnitTestMeta(String name) {
        var insertColumns = selectInsertColumnsContent(name);
        UnitTestMetaVO vo = new UnitTestMetaVO();
        List<UnitTestMetaVO.InsertTestMeta> list = insertColumns.stream().map(c -> {
            UnitTestMetaVO.InsertTestMeta meta = new UnitTestMetaVO.InsertTestMeta();
            meta.setDataType(mysqlType2JavaType(c.getDataType()));
            String varName = NameUtil.dashName2Camel(c.getColumnName());
            meta.setVarName(varName);
            meta.setVarValue(randomValueInsert(meta.getDataType()));
            meta.setVarValueUpdated(randomValueUpdate(meta.getDataType()));
            String accessorSuffix = Character.toUpperCase(varName.charAt(0)) + varName.substring(1);
            meta.setSetterName("set" + accessorSuffix);
            meta.setGetterName("get" + accessorSuffix);
            return meta;
        }).collect(Collectors.toList());
        vo.setInsertTestMetaList(list);
        return vo;
    }

    /**
     * 查询表数量
     *
     * @return
     */
    public int selectTableCount() {
        return schemaMapper.selectTableCount(schema);
    }

    /**
     * 生成依赖包文件
     *
     * @param param
     * @param randomDirName
     */
    public void genDependencyFiles(GenDependencyGoParam param, String randomDirName) {
        //在tmp目录创建子目录
        //在子目录创建基本结构
        File baseDir = new File(
                tmpDir +
                        File.separatorChar +
                        "mmcg8" +
                        File.separatorChar +
                        randomDirName +
                        File.separatorChar +
                        param.getArtifactName());
        baseDir.mkdirs();
        String basePackageFilePath = param.getBasePackage().replace('.', File.separatorChar);
        File mapperDir = new File(
                baseDir.getAbsolutePath()
                        + File.separatorChar
                        + "src"
                        + File.separatorChar +
                        "main" +
                        File.separatorChar +
                        "java" +
                        File.separatorChar +
                        basePackageFilePath +
                        File.separatorChar +
                        "mapper");
        mapperDir.mkdirs();
        File poDir = new File(
                baseDir.getAbsolutePath()
                        + File.separatorChar
                        + "src"
                        + File.separatorChar +
                        "main" +
                        File.separatorChar +
                        "java" +
                        File.separatorChar +
                        basePackageFilePath +
                        File.separatorChar +
                        "entity" +
                        File.separatorChar +
                        "po");
        poDir.mkdirs();
        File testBaseDir = new File(
                baseDir.getAbsolutePath()
                        + File.separatorChar
                        + "src"
                        + File.separatorChar +
                        "test" +
                        File.separatorChar +
                        "java" +
                        File.separatorChar +
                        basePackageFilePath);
        testBaseDir.mkdirs();
        File testMapperDir = new File(
                testBaseDir.getAbsolutePath()
                        + File.separatorChar +
                        "mapper");
        testMapperDir.mkdirs();
        File testResourcesDir=new File(
                testBaseDir.getAbsolutePath()+
                        File.separatorChar+
                        "resources"
        );
        testResourcesDir.mkdirs();

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        Template poTmpl,mapperTmpl,mapperXMLTmpl,testTmpl,pomTmpl,propertiesTmpl;
        try {
            var resourceURL=this.getClass().getClassLoader().getResource("freemarker");
            log.info("resource: file = {}, path = {}",resourceURL.getFile(),resourceURL.getPath());
            configuration.setDirectoryForTemplateLoading(new File(resourceURL.getFile()));
            poTmpl=configuration.getTemplate("po.ftl");
            mapperTmpl=configuration.getTemplate("mapper.ftl");
            mapperXMLTmpl=configuration.getTemplate("mapperXML.ftl");
            testTmpl=configuration.getTemplate("test.ftl");
            pomTmpl=configuration.getTemplate("pom.ftl");
            propertiesTmpl=configuration.getTemplate("prop.ftl");
        } catch (IOException e) {
            log.error("获取模板失败",e);
            throw new RuntimeException("获取模板失败");
        }
        //查询所有表
        var tables=schemaMapper.selectTablesBySchemaName(schema,null);
        tables.forEach(table->{
            //查询表的所有列
            var tableName=table.getTableName();
            var columns=schemaMapper.selectColumnsByTableName(schema,tableName);
            var pojoProperties=columns.stream().map(this::column2Property).collect(Collectors.toList());
            String className=NameUtil.dashName2BigCamel(tableName);
            PoTmplVO poParams=new PoTmplVO();
            poParams.setAttrs(pojoProperties);
            poParams.setClassName(className);
            poParams.setPackageName(param.getBasePackage()+".entity.po");
            poParams.setTableName(tableName);
            File poFile = new File(poDir.getAbsolutePath()+File.separatorChar+className+".java");
            Writer out= null;
            try {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(poFile)));
                poTmpl.process(poParams,out);
            } catch (TemplateException | IOException e) {
                log.error("模板渲染异常",e);
                throw new RuntimeException("模板渲染异常");
            }finally {
                if (out!=null){
                    try {
                        out.close();
                    } catch (IOException e) {
                        log.error("out关闭异常");
                    }
                }
            }
        });


        //给所有表创建mapper接口类、mapper.xml和PO类
        //给所有mapper接口创建对象的Test类
        //创建pom文件
        //创建测试用properties文件
    }
}
