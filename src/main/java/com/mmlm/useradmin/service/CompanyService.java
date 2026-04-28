package com.mmlm.useradmin.service;

import com.mmlm.useradmin.common.BusinessException;
import com.mmlm.useradmin.dto.company.CompanyResponse;
import com.mmlm.useradmin.dto.company.CompanySaveRequest;
import com.mmlm.useradmin.entity.SysCompany;
import com.mmlm.useradmin.repository.SysCompanyRepository;
import com.mmlm.useradmin.repository.SysUserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    private final SysCompanyRepository sysCompanyRepository;
    private final SysUserRepository sysUserRepository;

    public CompanyService(SysCompanyRepository sysCompanyRepository, SysUserRepository sysUserRepository) {
        this.sysCompanyRepository = sysCompanyRepository;
        this.sysUserRepository = sysUserRepository;
    }

    public List<CompanyResponse> list(String keyword, Integer status) {
        Specification<SysCompany> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            if (StringUtils.hasText(keyword)) {
                String likeValue = "%" + keyword.trim() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("name"), likeValue),
                        criteriaBuilder.like(root.get("code"), likeValue)
                ));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return sysCompanyRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CompanyResponse create(CompanySaveRequest request) {
        if (sysCompanyRepository.existsByCode(request.getCode().trim())) {
            throw new BusinessException("公司编码已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        SysCompany company = new SysCompany();
        company.setName(request.getName().trim());
        company.setCode(request.getCode().trim());
        company.setStatus(request.getStatus());
        company.setRemark(request.getRemark());
        company.setCreatedAt(now);
        company.setUpdatedAt(now);
        sysCompanyRepository.save(company);
        return toResponse(company);
    }

    @Transactional
    public CompanyResponse update(Long id, CompanySaveRequest request) {
        SysCompany company = sysCompanyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公司不存在"));
        if (sysCompanyRepository.existsByCodeAndIdNot(request.getCode().trim(), id)) {
            throw new BusinessException("公司编码已存在");
        }
        company.setName(request.getName().trim());
        company.setCode(request.getCode().trim());
        company.setStatus(request.getStatus());
        company.setRemark(request.getRemark());
        company.setUpdatedAt(LocalDateTime.now());
        sysCompanyRepository.save(company);
        return toResponse(company);
    }

    @Transactional
    public void delete(Long id) {
        SysCompany company = sysCompanyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公司不存在"));
        if (sysUserRepository.countByCompanyId(id) > 0) {
            throw new BusinessException("当前公司已被用户使用，不能删除");
        }
        sysCompanyRepository.delete(company);
    }

    private CompanyResponse toResponse(SysCompany company) {
        CompanyResponse response = new CompanyResponse();
        response.setId(company.getId());
        response.setName(company.getName());
        response.setCode(company.getCode());
        response.setStatus(company.getStatus());
        response.setRemark(company.getRemark());
        response.setCreatedAt(company.getCreatedAt());
        response.setUpdatedAt(company.getUpdatedAt());
        return response;
    }
}
