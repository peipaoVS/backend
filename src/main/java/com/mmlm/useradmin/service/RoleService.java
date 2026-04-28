package com.mmlm.useradmin.service;

import com.mmlm.useradmin.common.BusinessException;
import com.mmlm.useradmin.dto.role.RoleResponse;
import com.mmlm.useradmin.dto.role.RoleSaveRequest;
import com.mmlm.useradmin.entity.SysRole;
import com.mmlm.useradmin.repository.SysAgentModuleRoleRepository;
import com.mmlm.useradmin.repository.SysRoleMenuRepository;
import com.mmlm.useradmin.repository.SysRoleRepository;
import com.mmlm.useradmin.repository.SysUserRoleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final SysRoleRepository sysRoleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final SysRoleMenuRepository sysRoleMenuRepository;
    private final SysAgentModuleRoleRepository sysAgentModuleRoleRepository;

    public RoleService(SysRoleRepository sysRoleRepository,
                       SysUserRoleRepository sysUserRoleRepository,
                       SysRoleMenuRepository sysRoleMenuRepository,
                       SysAgentModuleRoleRepository sysAgentModuleRoleRepository) {
        this.sysRoleRepository = sysRoleRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.sysRoleMenuRepository = sysRoleMenuRepository;
        this.sysAgentModuleRoleRepository = sysAgentModuleRoleRepository;
    }

    public List<RoleResponse> list(String keyword, Integer status) {
        Specification<SysRole> specification = (root, query, criteriaBuilder) -> {
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
        return sysRoleRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoleResponse create(RoleSaveRequest request) {
        if (sysRoleRepository.existsByCode(request.getCode().trim())) {
            throw new BusinessException("Role code already exists");
        }
        LocalDateTime now = LocalDateTime.now();
        SysRole role = new SysRole();
        role.setName(request.getName().trim());
        role.setCode(request.getCode().trim());
        role.setStatus(request.getStatus());
        role.setRemark(request.getRemark());
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        sysRoleRepository.save(role);
        return toResponse(role);
    }

    @Transactional
    public RoleResponse update(Long id, RoleSaveRequest request) {
        SysRole role = sysRoleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Role not found"));
        if (sysRoleRepository.existsByCodeAndIdNot(request.getCode().trim(), id)) {
            throw new BusinessException("Role code already exists");
        }
        role.setName(request.getName().trim());
        role.setCode(request.getCode().trim());
        role.setStatus(request.getStatus());
        role.setRemark(request.getRemark());
        role.setUpdatedAt(LocalDateTime.now());
        sysRoleRepository.save(role);
        return toResponse(role);
    }

    @Transactional
    public void delete(Long id) {
        SysRole role = sysRoleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Role not found"));
        if ("ADMIN".equalsIgnoreCase(role.getCode())) {
            throw new BusinessException("Built-in admin role cannot be deleted");
        }
        if (!sysUserRoleRepository.findByRoleIdIn(Collections.singletonList(id)).isEmpty()) {
            throw new BusinessException("Current role is already assigned to users");
        }
        if (sysAgentModuleRoleRepository.countByRoleId(id) > 0) {
            throw new BusinessException("Current role is already bound to agent modules");
        }
        sysRoleMenuRepository.deleteByRoleId(id);
        sysRoleMenuRepository.flush();
        sysRoleRepository.deleteById(id);
    }

    private RoleResponse toResponse(SysRole role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        response.setCode(role.getCode());
        response.setStatus(role.getStatus());
        response.setRemark(role.getRemark());
        response.setCreatedAt(role.getCreatedAt());
        response.setUpdatedAt(role.getUpdatedAt());
        return response;
    }
}
