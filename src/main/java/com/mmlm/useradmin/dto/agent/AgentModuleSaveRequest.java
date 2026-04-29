package com.mmlm.useradmin.dto.agent;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class AgentModuleSaveRequest {

    @NotBlank(message = "模块名称不能为空")
    private String name;

    @NotBlank(message = "供应商不能为空")
    private String providerCode;

    @NotBlank(message = "模块类型不能为空")
    private String moduleType;

    @NotBlank(message = "基础模型不能为空")
    private String baseModel;

    @NotBlank(message = "API 域名不能为空")
    private String apiDomain;

    @NotBlank(message = "API Key 不能为空")
    private String apiKey;

    private String remark;

    private List<Long> roleIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public String getBaseModel() {
        return baseModel;
    }

    public void setBaseModel(String baseModel) {
        this.baseModel = baseModel;
    }

    public String getApiDomain() {
        return apiDomain;
    }

    public void setApiDomain(String apiDomain) {
        this.apiDomain = apiDomain;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
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
