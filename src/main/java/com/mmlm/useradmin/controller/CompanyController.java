package com.mmlm.useradmin.controller;

import com.mmlm.useradmin.common.ApiResponse;
import com.mmlm.useradmin.dto.company.CompanyResponse;
import com.mmlm.useradmin.dto.company.CompanySaveRequest;
import com.mmlm.useradmin.service.CompanyService;
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
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ApiResponse<List<CompanyResponse>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                   @RequestParam(value = "status", required = false) Integer status) {
        return ApiResponse.ok(companyService.list(keyword, status));
    }

    @PostMapping
    public ApiResponse<CompanyResponse> create(@Validated @RequestBody CompanySaveRequest request) {
        return ApiResponse.ok("创建成功", companyService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CompanyResponse> update(@PathVariable("id") Long id,
                                               @Validated @RequestBody CompanySaveRequest request) {
        return ApiResponse.ok("更新成功", companyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        companyService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
