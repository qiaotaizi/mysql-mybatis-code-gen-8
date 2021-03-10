package com.jaiz.web.gen.controller;

import com.jaiz.web.gen.eneity.GenDependencyGoParam;
import com.jaiz.web.gen.eneity.PojoPropertiesVO;
import com.jaiz.web.gen.eneity.TablesVO;
import com.jaiz.web.gen.service.IndexService;
import com.jaiz.web.gen.utils.NameUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

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
        var mapperNodes=indexService.selectMapperNodes(name);
        model.addAttribute("mapperNodes",mapperNodes);
        model.addAttribute("tableName",name);
        return "mapper";
    }

    @GetMapping("template")
    public String genTemplate(Model model,@RequestParam("table") String name){
        var unitTestMeta=indexService.selectUnitTestMeta(name);
        model.addAttribute("unitTestMeta",unitTestMeta);
        var mapperNodes=indexService.selectMapperNodes(name);
        model.addAttribute("mapperNodes",mapperNodes);
        String pojoClassName= NameUtil.dashName2BigCamel(name);
        model.addAttribute("pojoName",pojoClassName);
        model.addAttribute("tableName",name);
        List<PojoPropertiesVO> props=indexService.selectPojoProperties(name);
        model.addAttribute("props",props);
        return "templ";
    }

    /**
     * 生成完整依赖包页面
     * @return
     */
    @GetMapping("dependency")
    public String genDependency(Model model){
        //查询当前数据库包含的表数量
        var tableCount=indexService.selectTableCount();
        model.addAttribute("tableCount",tableCount);
        return "dependency";
    }



    @PostMapping("/dependency/go")
    public Mono<Void> genDependencyAndZipAndDownload(ServerHttpResponse response, @Validated GenDependencyGoParam param) throws IOException {
        log.info("下载参数, {}",param.toString());
        //uuid生成下载编号
        String tmpBaseDirName=UUID.randomUUID().toString();
        indexService.genDependencyFiles(param,tmpBaseDirName);
        //给artifact_name目录打压缩包

        //开启文件下载

        ZeroCopyHttpOutputMessage zeroCopyResponse = (ZeroCopyHttpOutputMessage) response;
        response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test.txt");
        response.getHeaders().setContentType(MediaType.MULTIPART_FORM_DATA);

        File file = new File("/tmp/test.txt");
        return zeroCopyResponse.writeWith(file, 0, file.length());
    }

}
