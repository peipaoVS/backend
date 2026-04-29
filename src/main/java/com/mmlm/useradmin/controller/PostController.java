package com.mmlm.useradmin.controller;

import com.mmlm.useradmin.common.ApiResponse;
import com.mmlm.useradmin.dto.post.PostResponse;
import com.mmlm.useradmin.dto.post.PostSaveRequest;
import com.mmlm.useradmin.service.PostService;
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
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ApiResponse<List<PostResponse>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                @RequestParam(value = "status", required = false) Integer status) {
        return ApiResponse.ok(postService.list(keyword, status));
    }

    @PostMapping
    public ApiResponse<PostResponse> create(@Validated @RequestBody PostSaveRequest request) {
        return ApiResponse.ok("创建成功", postService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PostResponse> update(@PathVariable("id") Long id,
                                            @Validated @RequestBody PostSaveRequest request) {
        return ApiResponse.ok("更新成功", postService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        postService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
