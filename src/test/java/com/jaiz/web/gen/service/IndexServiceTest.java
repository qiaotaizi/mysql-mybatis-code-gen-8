package com.jaiz.web.gen.service;

import com.jaiz.web.gen.BaseTest;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class IndexServiceTest extends BaseTest {

    @Autowired
    @Qualifier("schemaName")
    private String schema;

    @Test
    public void schemaNameTest() {

        Assertions.assertEquals("wifi_kpi_dev",schema, "比对错误");
    }
}
