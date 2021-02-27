package com.jaiz.web.gen.eneity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

@Getter
@Setter
public class MapperNodesVO {

    /**
     * 全部字段
     */
    private List<String> columnNames;

    /**
     * 去除id的字段
     */
    private List<MutablePair<String,String>> resultMapCpPairs;

    /**
     * 去除id、createTime、updateTime的字段
     */
    private List<MutablePair<String,String>> updateInsertCpPairs;

}
