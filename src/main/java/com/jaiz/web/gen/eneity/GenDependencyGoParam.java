package com.jaiz.web.gen.eneity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
public class GenDependencyGoParam {

    @NotBlank(message = "artifactName不能为空")
    private String artifactName;

    @NotBlank(message = "basePackage不能为空")
    private String basePackage;

}
