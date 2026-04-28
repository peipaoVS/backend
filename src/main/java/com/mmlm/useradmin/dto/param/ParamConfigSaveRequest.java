package com.mmlm.useradmin.dto.param;

import javax.validation.constraints.NotBlank;

public class ParamConfigSaveRequest {

    @NotBlank(message = "参数类型不能为空")
    private String paramType;

    @NotBlank(message = "参数名称不能为空")
    private String name;

    @NotBlank(message = "参数编号不能为空")
    private String code;

    @NotBlank(message = "参数值不能为空")
    private String paramValue;

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }
}
