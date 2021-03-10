package ${packageName};

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;

/**
* ${tableName}表对象
*/
@Getter
@Setter
@ToString
public class ${className} implements Serializable {

    private static final long serialVersionUID = -1L;

<#-- 循环类型及属性 -->
<#list attrs as attr>
    /**
     * ${attr.propertyDesc}
     */
    private ${attr.propertyType} ${attr.propertyName};
</#list>
}