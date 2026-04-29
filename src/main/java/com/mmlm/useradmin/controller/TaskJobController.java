package com.mmlm.useradmin.controller;

import com.mmlm.useradmin.common.ApiResponse;
import com.mmlm.useradmin.dto.task.TaskExportResponse;
import com.mmlm.useradmin.dto.task.TaskJobRequest;
import com.mmlm.useradmin.dto.task.TaskJobResponse;
import com.mmlm.useradmin.service.TaskJobService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/task-jobs")
public class TaskJobController {

    private final TaskJobService taskJobService;

    public TaskJobController(TaskJobService taskJobService) {
        this.taskJobService = taskJobService;
    }

    @GetMapping
    public ApiResponse<List<TaskJobResponse>> list() {
        return ApiResponse.ok(taskJobService.list());
    }

    @PutMapping("/{id}")
    public ApiResponse<TaskJobResponse> update(@PathVariable("id") Long id,
                                               @RequestBody TaskJobRequest request) {
        return ApiResponse.ok("更新成功", taskJobService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        taskJobService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }

    @GetMapping("/{id}/export")
    public ApiResponse<TaskExportResponse> export(@PathVariable("id") Long id) {
        return ApiResponse.ok(taskJobService.export(id));
    }
}
