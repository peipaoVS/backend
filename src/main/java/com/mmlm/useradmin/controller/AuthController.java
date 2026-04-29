package com.mmlm.useradmin.controller;

import com.mmlm.useradmin.common.ApiResponse;
import com.mmlm.useradmin.dto.auth.LoginRequest;
import com.mmlm.useradmin.dto.auth.LoginResponse;
import com.mmlm.useradmin.dto.auth.ThemeUpdateRequest;
import com.mmlm.useradmin.dto.auth.UserProfileResponse;
import com.mmlm.useradmin.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.auth.token-prefix}")
    private String tokenPrefix;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        return ApiResponse.ok("登录成功", authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me() {
        return ApiResponse.ok(authService.me());
    }

    @PutMapping("/theme")
    public ApiResponse<UserProfileResponse> updateTheme(@Validated @RequestBody ThemeUpdateRequest request) {
        return ApiResponse.ok("主题更新成功", authService.updateTheme(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(resolveToken(authorization));
        return ApiResponse.ok("退出成功", null);
    }

    private String resolveToken(String authorization) {
        if (authorization == null) {
            return null;
        }
        String prefix = tokenPrefix + " ";
        if (authorization.startsWith(prefix)) {
            return authorization.substring(prefix.length());
        }
        return authorization;
    }
}
