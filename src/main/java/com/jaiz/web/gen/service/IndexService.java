package com.jaiz.web.gen.service;

import com.jaiz.web.gen.constant.Const;
import com.jaiz.web.gen.eneity.*;
import com.jaiz.web.gen.mapper.SchemaMapper;
import com.jaiz.web.gen.utils.NameUtil;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
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
    private DataSourceProperties dataSourceProperties;

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
            case "Double" -> "123.0";
            case "Date" -> "format.parse(\"2021-01-01 03:02:01\")";
            default -> throw new RuntimeException("不识别的类型，请补充至源代码");
        };
    }

    private String randomValueUpdate(String propertyType) {
        return switch (propertyType) {
            case "Long" -> "321L";
            case "String" -> "\"StringRandom\"";
            case "Integer" -> "321";
            case "Double" -> "321.2";
            case "Date" -> "format.parse(\"2021-02-02 01:02:03\")";
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
            case "int", "smallint", "mediumint","tinyint" -> "Integer";
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
        List<InsertTestMetaVO> list = insertColumns.stream().map(c -> {
            InsertTestMetaVO meta = new InsertTestMetaVO();
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
                        Const.THIS_PROJECT_NAME_ABBR +
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
                baseDir.getAbsolutePath()
                        + File.separatorChar
                        + "src"
                        + File.separatorChar +
                        "test" +
                        File.separatorChar +
                        "resources"
        );
        testResourcesDir.mkdirs();

        //获取模板文件
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        Template poTmpl,mapperTmpl,mapperXMLTmpl,testTmpl,pomTmpl,propertiesTmpl,testMainClassTmpl;
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
            testMainClassTmpl=configuration.getTemplate("testMainClass.ftl");
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
            //生成PO
            String poClassName=NameUtil.dashName2BigCamel(tableName);
            String poPackageName=param.getBasePackage()+".entity.po";
            PoTmplVO poParams=new PoTmplVO();
            poParams.setAttrs(pojoProperties);
            poParams.setClassName(poClassName);
            poParams.setPackageName(poPackageName);
            poParams.setTableName(tableName);
            genFile(poDir.getAbsolutePath()+File.separatorChar+poClassName+".java",poTmpl,poParams);

            //生成mapper接口
            MapperTmplVO mapperParams=new MapperTmplVO();
            String mapperClassName=poClassName+"BaseMapper";
            mapperParams.setMapperClassName(mapperClassName);
            String mapperPackageName=param.getBasePackage()+".mapper";
            mapperParams.setPackageName(mapperPackageName);
            mapperParams.setPoClassName(poClassName);
            mapperParams.setPoPackageName(poPackageName);
            genFile(mapperDir.getAbsolutePath()+File.separatorChar+mapperClassName+".java",mapperTmpl,mapperParams);

            //生成mapper xml
            List<String> columnNames=columns.stream().map(ColumnsVO::getColumnName).collect(Collectors.toList());
            MapperXMLTmplVO mapperXMLTmplVO=new MapperXMLTmplVO();
            mapperXMLTmplVO.setColumnNames(columnNames);
            mapperXMLTmplVO.setMapperClassName(mapperClassName);
            mapperXMLTmplVO.setPackageName(mapperPackageName);
            mapperXMLTmplVO.setPoClassName(poClassName);
            var resultMapCpPairs = columns.stream().filter(c -> {
                //不要id
                return !c.getColumnName().equals("ID");
            }).map(this::column2CpPair).collect(Collectors.toList());
            var updateInsertCpPairs = columns.stream().filter(c -> {
                //不要id，updateTime,createTime
                return !c.getColumnName().equals("ID") && !c.getColumnName().equals("UPDATE_TIME") && !c.getColumnName().equals("CREATE_TIME");
            }).map(this::column2CpPair).collect(Collectors.toList());
            mapperXMLTmplVO.setResultMapCpPairs(resultMapCpPairs);
            mapperXMLTmplVO.setTableName(tableName);
            mapperXMLTmplVO.setUpdateInsertCpPairs(updateInsertCpPairs);
            mapperXMLTmplVO.setPoPackageName(poPackageName);
            genFile(mapperDir.getAbsolutePath()+File.separatorChar+mapperClassName+".xml",mapperXMLTmpl,mapperXMLTmplVO);

            //创建test类
            TestTmplVO testParam=new TestTmplVO();
            List<InsertTestMetaVO> insertTestMetaVOS = columns.stream().filter(c -> {
                //不要id，updateTime,createTime
                return !c.getColumnName().equals("ID") && !c.getColumnName().equals("UPDATE_TIME") && !c.getColumnName().equals("CREATE_TIME");
            }).map(c -> {
                InsertTestMetaVO meta = new InsertTestMetaVO();
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
            testParam.setInsertTestMetaList(insertTestMetaVOS);
            testParam.setMapperClassName(mapperClassName);
            testParam.setMapperPackageName(mapperPackageName);
            String testClassName=poClassName+"BaseTest";
            testParam.setTestClassName(testClassName);
            testParam.setPoClassName(poClassName);
            testParam.setPoPackageName(poPackageName);
            String artifactBigCamelName=NameUtil.artifactName2BigCamel(param.getArtifactName());
            log.info("artifactBigCamelName = {}",artifactBigCamelName);
            testParam.setArtifactBigCamelName(artifactBigCamelName);
            testParam.setBasePackage(param.getBasePackage());
            genFile(testMapperDir.getAbsolutePath()+File.separatorChar+testClassName+".java",testTmpl,testParam);

            //创建pom文件
            PomTmplVO pomParam=new PomTmplVO();
            pomParam.setBasePackage(param.getBasePackage());
            pomParam.setArtifactId(param.getArtifactName());
            genFile(baseDir.getAbsolutePath()+File.separatorChar+"pom.xml",pomTmpl,pomParam);

            //创建properties文件
            PropTmplVO propParam=new PropTmplVO();
            propParam.setSpringDatasourceUrl(dataSourceProperties.getUrl());
            propParam.setSpringDatasourceUserName(dataSourceProperties.getUsername());
            propParam.setSpringDatasourcePassword(dataSourceProperties.getPassword());
            genFile(testResourcesDir.getAbsolutePath()+File.separatorChar+"application.properties",propertiesTmpl,propParam);

            //创建TestApplication文件
            TestAppTmplVO testAppTmplParam=new TestAppTmplVO();
            testAppTmplParam.setBasePackage(param.getBasePackage());
            testAppTmplParam.setArtifactBigCamelName(artifactBigCamelName);
            genFile(testBaseDir.getAbsolutePath()+File.separatorChar+artifactBigCamelName+"TestApplication.java",testMainClassTmpl,testAppTmplParam);
        });
    }

    /**
     * 根据template文件和参数在指定位置生成文件
     * @param pathName
     * @param poTmpl
     */
    private void genFile(String pathName, Template poTmpl,Object params) {
        Writer out= null;
        File poFile = new File(pathName);
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(poFile)));
            poTmpl.process(params,out);
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
    }
}
