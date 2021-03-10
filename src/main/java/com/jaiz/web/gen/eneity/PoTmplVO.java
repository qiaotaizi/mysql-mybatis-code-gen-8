package com.jaiz.web.gen.eneity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PoTmplVO {

    private String packageName;

    private String tableName;

    private String className;

    private List<PojoPropertiesVO> attrs;

}
