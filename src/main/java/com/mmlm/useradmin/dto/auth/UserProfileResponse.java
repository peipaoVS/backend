package com.mmlm.useradmin.dto.auth;

import java.util.List;

public class UserProfileResponse {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Long companyId;
    private String companyName;
    private String theme;
    private List<String> roleNames;
    private List<String> postNames;
    private List<UserMenuResponse> menus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
    }

    public List<String> getPostNames() {
        return postNames;
    }

    public void setPostNames(List<String> postNames) {
        this.postNames = postNames;
    }

    public List<UserMenuResponse> getMenus() {
        return menus;
    }

    public void setMenus(List<UserMenuResponse> menus) {
        this.menus = menus;
    }
}
