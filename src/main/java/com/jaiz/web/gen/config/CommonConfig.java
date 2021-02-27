package com.jaiz.web.gen.config;

import com.jaiz.web.gen.mapper.SchemaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

    @Autowired
    private DataSourceProperties dataSourceProperties;


    /**
     * jdbc:mysql://wifi.kpi.mysql:13306/wifi_kpi_dev?xxx=xxx
     * @return
     */
    @Bean("schemaName")
    public String schemaName() {
        String url=dataSourceProperties.getUrl();
        String hostPortTableNameParams=url.substring(url.indexOf("://")+3);
        // wifi.kpi.mysql:13306/wifi_kpi_dev?xxx=xxx
        int paramStartIndex=hostPortTableNameParams.indexOf("?");
        String schemaName=hostPortTableNameParams.substring(hostPortTableNameParams.indexOf("/")+1,paramStartIndex==-1?hostPortTableNameParams.length():paramStartIndex);
        return schemaName;
    }
}
