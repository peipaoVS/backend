package com.mmlm.useradmin.dto.chat;

import java.time.LocalDateTime;

public class ChatResponse {

    private String answer;
    private LocalDateTime time;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
