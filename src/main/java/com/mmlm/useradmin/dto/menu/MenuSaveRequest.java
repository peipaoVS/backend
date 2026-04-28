package com.mmlm.useradmin.dto.menu;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class MenuSaveRequest {

    @NotBlank(message = "菜单名称不能为空")
    private String name;

    @NotBlank(message = "菜单编码不能为空")
    private String code;

    @NotBlank(message = "所属栏目不能为空")
    private String section;

    @NotBlank(message = "菜单路径不能为空")
    private String path;

    @NotNull(message = "排序值不能为空")
    private Integer sortOrder;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;

    private List<Long> roleIds;

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

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
