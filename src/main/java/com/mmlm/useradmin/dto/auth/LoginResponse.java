package com.mmlm.useradmin.dto.auth;

public class LoginResponse {

    private String token;
    private UserProfileResponse user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserProfileResponse getUser() {
        return user;
    }

    public void setUser(UserProfileResponse user) {
        this.user = user;
    }
}
