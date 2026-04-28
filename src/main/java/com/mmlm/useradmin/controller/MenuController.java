package com.mmlm.useradmin.controller;

import com.mmlm.useradmin.common.ApiResponse;
import com.mmlm.useradmin.dto.menu.MenuResponse;
import com.mmlm.useradmin.dto.menu.MenuSaveRequest;
import com.mmlm.useradmin.service.MenuService;
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
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ApiResponse<List<MenuResponse>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                @RequestParam(value = "status", required = false) Integer status) {
        return ApiResponse.ok(menuService.list(keyword, status));
    }

    @PostMapping
    public ApiResponse<MenuResponse> create(@Validated @RequestBody MenuSaveRequest request) {
        return ApiResponse.ok("创建成功", menuService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<MenuResponse> update(@PathVariable("id") Long id,
                                            @Validated @RequestBody MenuSaveRequest request) {
        return ApiResponse.ok("更新成功", menuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        menuService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
