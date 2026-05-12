package com.mmlm.useradmin.dto.session;

import javax.validation.constraints.NotNull;

public class SessionCodeSaveRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String code;

    @NotNull(message = "岗位ID不能为空")
    private Long postId;

    private Integer isActive = 1;

    private String remark;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
