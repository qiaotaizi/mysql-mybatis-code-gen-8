package ${packageName};

import ${poPackageName}.${poClassName};
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ${mapperClassName} {

    /**
     * 全量insert
     */
    int insert(${poClassName} param);

    /**
     * 根据ID查询
     */
    ${poClassName} selectById(@Param("id")Long id);

    /**
     * 根据ID对非空字段更新
     */
    int updateSelective(${poClassName} param);

    /**
     * 根据id删除记录
     */
    int deleteById(@Param("id")Long id);

}