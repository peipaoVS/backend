package com.mmlm.useradmin.auth;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenSessionService {

    private final Map<String, TokenSession> sessionStore = new ConcurrentHashMap<String, TokenSession>();

    public String create(Long userId, String username) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessionStore.put(token, new TokenSession(userId, username, LocalDateTime.now()));
        return token;
    }

    public TokenSession get(String token) {
        return sessionStore.get(token);
    }

    public void remove(String token) {
        if (token != null) {
            sessionStore.remove(token);
        }
    }
}
