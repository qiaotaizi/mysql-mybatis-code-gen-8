package com.jaiz.web.gen.mapper;

import com.jaiz.web.gen.eneity.ColumnsVO;
import com.jaiz.web.gen.eneity.TablesVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SchemaMapper {


    List<TablesVO> selectTablesBySchemaName(
            @Param("schemaName") String schemaName,
            @Param("name")String name
            );

    /**
     * 根据表名查询所有字段
     * @param name
     * @return
     */
    List<ColumnsVO> selectColumnsByTableName(@Param("schema") String schema, @Param("name") String name);

    /**
     * 统计表数量
     * @param schema
     * @return
     */
    int selectTableCount(@Param("schemaName") String schema);
}
