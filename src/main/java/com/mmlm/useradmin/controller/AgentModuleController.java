package com.mmlm.useradmin.controller;

import com.mmlm.useradmin.common.ApiResponse;
import com.mmlm.useradmin.dto.agent.AgentModuleResponse;
import com.mmlm.useradmin.dto.agent.AgentModuleSaveRequest;
import com.mmlm.useradmin.service.AgentModuleService;
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
@RequestMapping("/api/agent-modules")
public class AgentModuleController {

    private final AgentModuleService agentModuleService;

    public AgentModuleController(AgentModuleService agentModuleService) {
        this.agentModuleService = agentModuleService;
    }

    @GetMapping
    public ApiResponse<List<AgentModuleResponse>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam(value = "moduleType", required = false) String moduleType) {
        return ApiResponse.ok(agentModuleService.list(keyword, moduleType));
    }

    @GetMapping("/available")
    public ApiResponse<List<AgentModuleResponse>> listAvailable(@RequestParam(value = "moduleType", required = false) String moduleType) {
        return ApiResponse.ok(agentModuleService.listAvailableForCurrentUser(moduleType));
    }

    @PostMapping
    public ApiResponse<AgentModuleResponse> create(@Validated @RequestBody AgentModuleSaveRequest request) {
        return ApiResponse.ok("创建成功", agentModuleService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AgentModuleResponse> update(@PathVariable("id") Long id,
                                                   @Validated @RequestBody AgentModuleSaveRequest request) {
        return ApiResponse.ok("更新成功", agentModuleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        agentModuleService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
