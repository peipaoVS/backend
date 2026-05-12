package com.mmlm.useradmin.dto.session;

import com.mmlm.useradmin.dto.user.UserSimpleResponse;

import java.util.List;

public class PostTreeResponse {
    private Long postId;
    private String postName;
    private Long leaderUserId;
    private List<UserSimpleResponse> users;
    private List<SessionCodeResponse> sessionCodes;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public Long getLeaderUserId() {
        return leaderUserId;
    }

    public void setLeaderUserId(Long leaderUserId) {
        this.leaderUserId = leaderUserId;
    }

    public List<UserSimpleResponse> getUsers() {
        return users;
    }

    public void setUsers(List<UserSimpleResponse> users) {
        this.users = users;
    }

    public List<SessionCodeResponse> getSessionCodes() {
        return sessionCodes;
    }

    public void setSessionCodes(List<SessionCodeResponse> sessionCodes) {
        this.sessionCodes = sessionCodes;
    }
}
