package com.jaiz.web.gen.eneity;

import lombok.Getter;
import lombok.Setter;

/**
 * 列VO
 */
@Getter
@Setter
public class ColumnsVO {


    private String columnName;

    private String dataType;

    private Integer numericPrecision;

    private Integer numericScale;

    private String columnComment;

    private String columnKey;

    private Integer ordinalPosition;
}
