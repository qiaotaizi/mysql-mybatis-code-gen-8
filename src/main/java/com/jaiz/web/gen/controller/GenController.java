package com.jaiz.web.gen.controller;

import com.jaiz.web.gen.eneity.MapperNodesVO;
import com.jaiz.web.gen.eneity.PojoPropertiesVO;
import com.jaiz.web.gen.eneity.TablesVO;
import com.jaiz.web.gen.service.IndexService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("gen")
public class GenController {

    @Autowired
    private IndexService indexService;


    /**
     * 首页列举表
     * @param model
     * @param name 支持模糊表名查询
     * @return
     */
    @GetMapping("index")
    public String index(Model model,@RequestParam(value = "name",required = false)String name){
        List<TablesVO> tables=indexService.showIndex(name);
        model.addAttribute("tables",tables);
        return "index";
    }

    /**
     * 生成pojo属性
     * @param model
     * @param name 表名
     * @return
     */
    @GetMapping("pojo")
    public String genPojo(Model model,@RequestParam("table") String name){
        List<PojoPropertiesVO> props=indexService.selectPojoProperties(name);
        model.addAttribute("props",props);
        model.addAttribute("tableName",name);
        return "pojo";
    }

    /**
     * 生成Mapper属性
     * @param model
     * @param name
     * @return
     */
    @GetMapping("mapper")
    public String genMapper(Model model,@RequestParam("table") String name){
        //MapperNodesVO mapperNodes=indexService.selectMapperNodes(name);
        //var columnPropertyPairs=indexService.selectInsertColumnsContent(name);
        //model.addAttribute("cpPairs",columnPropertyPairs);
        var mapperNodes=indexService.selectMapperNodes(name);
        model.addAttribute("mapperNodes",mapperNodes);
        model.addAttribute("tableName",name);
        return "mapper";
    }

}
