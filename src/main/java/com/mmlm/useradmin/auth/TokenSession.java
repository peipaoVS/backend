package com.mmlm.useradmin.auth;

import java.time.LocalDateTime;

public class TokenSession {

    private final Long userId;
    private final String username;
    private final LocalDateTime loginTime;

    public TokenSession(Long userId, String username, LocalDateTime loginTime) {
        this.userId = userId;
        this.username = username;
        this.loginTime = loginTime;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }
}
