<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${packageName}.${mapperClassName}">
    <!-- 全量字段 -->
    <sql id="fullColumns">
        <#list columnNames as column>
            ${column}<#if column_has_next>,</#if>
        </#list>
    </sql>

    <!-- 全量字段resultMap -->
    <resultMap id="fullMap" type="${poPackageName}.${poClassName}">
        <id column="ID" property="id" />
        <#list resultMapCpPairs as cpPair>
            <result column="${cpPair.left}" property="${cpPair.right}" />
        </#list>
    </resultMap>

    <!-- 根据id查询 -->
    <select id="selectById" resultMap="fullMap">
        SELECT <include refid="fullColumns" /> FROM #{tableName} WHERE ID = #\{id\}
    </select>

    <!-- 根据id更新非空字段 -->
    <update id="updateSelective">
        UPDATE ${tableName}
        <set>
            <#list updateInsertCpPairs as cpPair>
                <if test="#\{${cpPair.right}\} != null">
                    ${cpPair.left} = #\{${cpPair.right}\},
                </if>
            </#list>
        </set>
        WHERE ID=#\{id\}
    </update>

    <!-- 全量insert -->
    <insert id="insert" useGeneratedKeys="true" keyProperty="id" keyColumn="ID">
        INSERT INTO ${tableName}
        (
        <#list updateInsertCpPairs as cpPair>
                ${cpPair.left}<#if cpPair_has_next>,</#if>
        </#list>
        ) VALUES (
        <#list updateInsertCpPairs as cpPair>
            #\{${cpPair.right}\}<#if cpPair_has_next>,</#if>
        </#list>
        )
    </insert>

    <!-- 根据id删除 -->
    <delete id="deleteById">
        DELETE FROM ${tableName} WHERE ID = #{id}
    </delete>
</mapper>
