package com.mmlm.useradmin.controller;

import com.mmlm.useradmin.common.ApiResponse;
import com.mmlm.useradmin.dto.user.UserResponse;
import com.mmlm.useradmin.dto.user.UserSaveRequest;
import com.mmlm.useradmin.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                @RequestParam(value = "status", required = false) Integer status) {
        return ApiResponse.ok(userService.list(keyword, status));
    }

    @PostMapping
    public ApiResponse<UserResponse> create(@Validated @RequestBody UserSaveRequest request) {
        return ApiResponse.ok("创建成功", userService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable("id") Long id,
                                            @Validated @RequestBody UserSaveRequest request) {
        return ApiResponse.ok("更新成功", userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
