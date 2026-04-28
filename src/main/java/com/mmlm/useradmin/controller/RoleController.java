package com.mmlm.useradmin.controller;

import com.mmlm.useradmin.common.ApiResponse;
import com.mmlm.useradmin.dto.role.RoleResponse;
import com.mmlm.useradmin.dto.role.RoleSaveRequest;
import com.mmlm.useradmin.service.RoleService;
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
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                @RequestParam(value = "status", required = false) Integer status) {
        return ApiResponse.ok(roleService.list(keyword, status));
    }

    @PostMapping
    public ApiResponse<RoleResponse> create(@Validated @RequestBody RoleSaveRequest request) {
        return ApiResponse.ok("创建成功", roleService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoleResponse> update(@PathVariable("id") Long id,
                                            @Validated @RequestBody RoleSaveRequest request) {
        return ApiResponse.ok("更新成功", roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        roleService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
