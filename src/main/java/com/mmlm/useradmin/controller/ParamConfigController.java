package com.mmlm.useradmin.controller;

import com.mmlm.useradmin.common.ApiResponse;
import com.mmlm.useradmin.dto.param.ParamConfigResponse;
import com.mmlm.useradmin.dto.param.ParamConfigSaveRequest;
import com.mmlm.useradmin.service.ParamConfigService;
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
@RequestMapping("/api/param-configs")
public class ParamConfigController {

    private final ParamConfigService paramConfigService;

    public ParamConfigController(ParamConfigService paramConfigService) {
        this.paramConfigService = paramConfigService;
    }

    @GetMapping
    public ApiResponse<List<ParamConfigResponse>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam(value = "paramType", required = false) String paramType) {
        return ApiResponse.ok(paramConfigService.list(keyword, paramType));
    }

    @PostMapping
    public ApiResponse<ParamConfigResponse> create(@Validated @RequestBody ParamConfigSaveRequest request) {
        return ApiResponse.ok("创建成功", paramConfigService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ParamConfigResponse> update(@PathVariable("id") Long id,
                                                   @Validated @RequestBody ParamConfigSaveRequest request) {
        return ApiResponse.ok("更新成功", paramConfigService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        paramConfigService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
