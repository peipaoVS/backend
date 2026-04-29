package com.mmlm.useradmin.service;

import com.mmlm.useradmin.common.BusinessException;
import com.mmlm.useradmin.dto.param.ParamConfigResponse;
import com.mmlm.useradmin.dto.param.ParamConfigSaveRequest;
import com.mmlm.useradmin.entity.SysParamConfig;
import com.mmlm.useradmin.repository.SysParamConfigRepository;
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
public class ParamConfigService {

    private final SysParamConfigRepository sysParamConfigRepository;

    public ParamConfigService(SysParamConfigRepository sysParamConfigRepository) {
        this.sysParamConfigRepository = sysParamConfigRepository;
    }

    public List<ParamConfigResponse> list(String keyword, String paramType) {
        Specification<SysParamConfig> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            if (StringUtils.hasText(keyword)) {
                String likeValue = "%" + keyword.trim() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("name"), likeValue),
                        criteriaBuilder.like(root.get("code"), likeValue),
                        criteriaBuilder.like(root.get("paramType"), likeValue)
                ));
            }
            if (StringUtils.hasText(paramType)) {
                predicates.add(criteriaBuilder.equal(root.get("paramType"), paramType.trim()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return sysParamConfigRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "updatedAt")
                        .and(Sort.by(Sort.Direction.DESC, "id")))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParamConfigResponse create(ParamConfigSaveRequest request) {
        if (sysParamConfigRepository.existsByCode(request.getCode().trim())) {
            throw new BusinessException("参数编号已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        SysParamConfig config = new SysParamConfig();
        config.setParamType(request.getParamType().trim());
        config.setName(request.getName().trim());
        config.setCode(request.getCode().trim());
        config.setParamValue(request.getParamValue().trim());
        config.setCreatedAt(now);
        config.setUpdatedAt(now);
        sysParamConfigRepository.save(config);
        return toResponse(config);
    }

    @Transactional
    public ParamConfigResponse update(Long id, ParamConfigSaveRequest request) {
        SysParamConfig config = sysParamConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("参数配置不存在"));

        if (sysParamConfigRepository.existsByCodeAndIdNot(request.getCode().trim(), id)) {
            throw new BusinessException("参数编号已存在");
        }

        config.setParamType(request.getParamType().trim());
        config.setName(request.getName().trim());
        config.setCode(request.getCode().trim());
        config.setParamValue(request.getParamValue().trim());
        config.setUpdatedAt(LocalDateTime.now());
        sysParamConfigRepository.save(config);
        return toResponse(config);
    }

    @Transactional
    public void delete(Long id) {
        SysParamConfig config = sysParamConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("参数配置不存在"));
        sysParamConfigRepository.delete(config);
    }

    private ParamConfigResponse toResponse(SysParamConfig config) {
        ParamConfigResponse response = new ParamConfigResponse();
        response.setId(config.getId());
        response.setParamType(config.getParamType());
        response.setName(config.getName());
        response.setCode(config.getCode());
        response.setParamValue(config.getParamValue());
        response.setCreatedAt(config.getCreatedAt());
        response.setUpdatedAt(config.getUpdatedAt());
        return response;
    }
}
