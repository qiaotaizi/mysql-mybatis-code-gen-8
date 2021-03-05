package com.jaiz.web.gen.eneity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UnitTestMetaVO {

    private List<InsertTestMeta> insertTestMetaList;

    @Getter
    @Setter
    public static class InsertTestMeta{
        private String dataType;
        private String varName;
        private String varValue;
        private String varValueUpdated;
        private String setterName;
        private String getterName;
    }
}
