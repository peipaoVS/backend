package com.mmlm.useradmin.auth;

import com.mmlm.useradmin.common.ApiResponse;
import com.mmlm.useradmin.common.AuthContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenSessionService tokenSessionService;
    private final ObjectMapper objectMapper;

    @Value("${app.auth.token-header}")
    private String tokenHeader;

    @Value("${app.auth.token-prefix}")
    private String tokenPrefix;

    public AuthInterceptor(TokenSessionService tokenSessionService, ObjectMapper objectMapper) {
        this.tokenSessionService = tokenSessionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader(tokenHeader);
        String token = resolveToken(authHeader);
        TokenSession session = tokenSessionService.get(token);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail("未登录或登录状态已失效")));
            return false;
        }

        AuthContext.set(session.getUserId(), session.getUsername());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        AuthContext.clear();
    }

    private String resolveToken(String authHeader) {
        if (!StringUtils.hasText(authHeader)) {
            return null;
        }
        String prefix = tokenPrefix + " ";
        if (authHeader.startsWith(prefix)) {
            return authHeader.substring(prefix.length());
        }
        return authHeader;
    }
}
