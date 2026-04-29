package com.mmlm.useradmin.dto.chat;

import javax.validation.constraints.NotBlank;

public class ChatRequest {

    @NotBlank(message = "请输入问题")
    private String message;

    private String model;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
