package com.jaiz.web.gen.eneity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestTmplVO {

    private String mapperPackageName;

    private String poPackageName;

    private String poClassName;

    private String testClassName;

    private String mapperClassName;

    private List<InsertTestMetaVO> insertTestMetaList;

    private String artifactBigCamelName;

    private String basePackage;

}
