package com.mmlm.useradmin.dto.post;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class PostSaveRequest {

    @NotBlank(message = "岗位名称不能为空")
    private String name;

    @NotBlank(message = "岗位编码不能为空")
    private String code;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
