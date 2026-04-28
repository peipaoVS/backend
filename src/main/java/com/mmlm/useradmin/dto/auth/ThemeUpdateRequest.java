package com.mmlm.useradmin.dto.auth;

import javax.validation.constraints.NotBlank;

public class ThemeUpdateRequest {

    @NotBlank(message = "主题不能为空")
    private String theme;

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
