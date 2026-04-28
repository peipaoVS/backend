package com.mmlm.useradmin.service;

import com.mmlm.useradmin.common.BusinessException;
import com.mmlm.useradmin.dto.post.PostResponse;
import com.mmlm.useradmin.dto.post.PostSaveRequest;
import com.mmlm.useradmin.entity.SysPost;
import com.mmlm.useradmin.repository.SysPostRepository;
import com.mmlm.useradmin.repository.SysUserPostRepository;
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
public class PostService {

    private final SysPostRepository sysPostRepository;
    private final SysUserPostRepository sysUserPostRepository;

    public PostService(SysPostRepository sysPostRepository, SysUserPostRepository sysUserPostRepository) {
        this.sysPostRepository = sysPostRepository;
        this.sysUserPostRepository = sysUserPostRepository;
    }

    public List<PostResponse> list(String keyword, Integer status) {
        Specification<SysPost> specification = (root, query, criteriaBuilder) -> {
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
        return sysPostRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostResponse create(PostSaveRequest request) {
        if (sysPostRepository.existsByCode(request.getCode())) {
            throw new BusinessException("岗位编码已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        SysPost post = new SysPost();
        post.setName(request.getName().trim());
        post.setCode(request.getCode().trim());
        post.setStatus(request.getStatus());
        post.setRemark(request.getRemark());
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        sysPostRepository.save(post);
        return toResponse(post);
    }

    @Transactional
    public PostResponse update(Long id, PostSaveRequest request) {
        SysPost post = sysPostRepository.findById(id)
                .orElseThrow(() -> new BusinessException("岗位不存在"));
        if (sysPostRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new BusinessException("岗位编码已存在");
        }
        post.setName(request.getName().trim());
        post.setCode(request.getCode().trim());
        post.setStatus(request.getStatus());
        post.setRemark(request.getRemark());
        post.setUpdatedAt(LocalDateTime.now());
        sysPostRepository.save(post);
        return toResponse(post);
    }

    @Transactional
    public void delete(Long id) {
        SysPost post = sysPostRepository.findById(id)
                .orElseThrow(() -> new BusinessException("岗位不存在"));
        if ("CEO".equalsIgnoreCase(post.getCode())) {
            throw new BusinessException("内置岗位不允许删除");
        }
        if (!sysUserPostRepository.findByPostIdIn(java.util.Collections.singletonList(id)).isEmpty()) {
            throw new BusinessException("当前岗位已被用户使用，不能删除");
        }
        sysPostRepository.deleteById(id);
    }

    private PostResponse toResponse(SysPost post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setName(post.getName());
        response.setCode(post.getCode());
        response.setStatus(post.getStatus());
        response.setRemark(post.getRemark());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        return response;
    }
}
