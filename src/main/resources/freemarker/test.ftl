package ${mapperPackageName};

import ${poPackageName}.${poClassName};
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.apache.commons.lang3.time.FastDateFormat;
import java.util.Date;
import java.text.ParseException;
import ${basePackage}.${artifactBigCamelName}TestApplication;

@SpringBootTest(classes = ${artifactBigCamelName}TestApplication.class)
@RunWith(SpringRunner.class)
public class ${testClassName} {

    @Autowired
    private ${mapperClassName} theMapper;

    private FastDateFormat format =FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    @Test
    @Transactional
    @Rollback
    public void baseCrudTest() throws ParseException{
        //初始化测试字段
        <#list insertTestMetaList as insertMeta>
            ${insertMeta.dataType} ${insertMeta.varName}Insert = ${insertMeta.varValue};
        </#list>
        //new对象并set
        ${poClassName} insertParam = new ${poClassName}();
        <#list insertTestMetaList as insertMeta>
            insertParam.${insertMeta.setterName}(${insertMeta.varName}Insert);
        </#list>
        //测试insert
        theMapper.insert(insertParam);
        //检查自增id非空
        Long id=insertParam.getId();
        Assert.assertNotNull("自增id为空",id);
        //测试selectById
        ${poClassName} selected = theMapper.selectById(id);
        //检查对象非空
        Assert.assertNotNull("数据库记录为空",selected);
        //字段比对
        <#list insertTestMetaList as insertMeta>
            Assert.assertEquals("${insertMeta.varName}比对失败", ${insertMeta.varName}Insert , selected.${insertMeta.getterName}());
        </#list>
        //初始化update测试字段
        <#list insertTestMetaList as insertMeta>
            ${insertMeta.dataType} ${insertMeta.varName}Update = ${insertMeta.varValueUpdated};
        </#list>
        //new对象并set
        ${poClassName} updateParam = new ${poClassName}();
        <#list insertTestMetaList as insertMeta>
            updateParam.${insertMeta.setterName}(${insertMeta.varName}Update);
        </#list>
        updateParam.setId(id);
        //测试update
        int updateColumn=theMapper.updateSelective(updateParam);
        Assert.assertEquals("更新影响行数错误",1,updateColumn);
        //查询更新后的结果
        ${poClassName} updated = theMapper.selectById(id);
        //检查对象非空
        Assert.assertNotNull("更新后查询失败",updated);
        //字段比对
        <#list insertTestMetaList as insertMeta>
            Assert.assertEquals("${insertMeta.varName}比对失败", ${insertMeta.varName}Update , updated.${insertMeta.getterName}());
        </#list>
        //测试deleteById
        theMapper.deleteById(id);
        //测试是否能在删除后查询出来
        ${poClassName} deleted = theMapper.selectById(id);
        Assert.assertNull("执行delete后不应查出数据",deleted);
    }

}
