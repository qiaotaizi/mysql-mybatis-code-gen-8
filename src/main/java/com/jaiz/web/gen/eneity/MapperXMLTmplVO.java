package com.jaiz.web.gen.eneity;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

@Getter
@Setter
public class MapperXMLTmplVO {

    private String packageName;

    private String mapperClassName;

    private List<String> columnNames;

    private String poPackageName;


    private String poClassName;

    private List<MutablePair<String,String>> resultMapCpPairs;

    private String tableName;

    private List<MutablePair<String,String>> updateInsertCpPairs;

}
