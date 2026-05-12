package com.mmlm.useradmin.controller;

import com.mmlm.useradmin.common.ApiResponse;
import com.mmlm.useradmin.dto.session.PostTreeResponse;
import com.mmlm.useradmin.dto.session.SessionCodeResponse;
import com.mmlm.useradmin.dto.session.SessionCodeSaveRequest;
import com.mmlm.useradmin.service.SessionCodeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/session-codes")
public class SessionCodeController {

    private final SessionCodeService sessionCodeService;

    public SessionCodeController(SessionCodeService sessionCodeService) {
        this.sessionCodeService = sessionCodeService;
    }

    // 获取岗位树（包含用户和会话编码）
    @GetMapping("/post-tree")
    public ApiResponse<List<PostTreeResponse>> getPostTree() {
        return ApiResponse.ok(sessionCodeService.getPostTree());
    }

    // 根据ID获取会话编码
    @GetMapping("/{id}")
    public ApiResponse<SessionCodeResponse> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(sessionCodeService.getById(id));
    }

    // 生成会话编码
    @PostMapping("/generate")
    public ApiResponse<SessionCodeResponse> generateCode(@RequestBody SessionCodeSaveRequest request) {
        return ApiResponse.ok("生成成功", sessionCodeService.generateCode(request.getUserId(), request.getPostId()));
    }

    // 修改会话编码指向的用户
    @PutMapping("/{id}/user")
    public ApiResponse<SessionCodeResponse> updateCodeUser(@PathVariable("id") Long id,
                                                        @RequestBody SessionCodeSaveRequest request) {
        return ApiResponse.ok("修改成功", sessionCodeService.updateCodeUser(id, request.getUserId()));
    }

    // 设置岗位负责人
    @PutMapping("/post/{postId}/leader")
    public ApiResponse<Void> setLeader(@PathVariable("postId") Long postId,
                                      @RequestBody SessionCodeSaveRequest request) {
        sessionCodeService.setLeader(postId, request.getUserId());
        return ApiResponse.ok("设置成功", null);
    }

    // 删除会话编码
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCode(@PathVariable("id") Long id) {
        sessionCodeService.deleteCode(id);
        return ApiResponse.ok("删除成功", null);
    }
}
