package com.mmlm.useradmin.service;

import com.mmlm.useradmin.common.BusinessException;
import com.mmlm.useradmin.dto.user.UserResponse;
import com.mmlm.useradmin.dto.user.UserSaveRequest;
import com.mmlm.useradmin.entity.SysCompany;
import com.mmlm.useradmin.entity.SysPost;
import com.mmlm.useradmin.entity.SysRole;
import com.mmlm.useradmin.entity.SysUser;
import com.mmlm.useradmin.entity.SysUserPost;
import com.mmlm.useradmin.entity.SysUserRole;
import com.mmlm.useradmin.repository.SysCompanyRepository;
import com.mmlm.useradmin.repository.SysPostRepository;
import com.mmlm.useradmin.repository.SysRoleRepository;
import com.mmlm.useradmin.repository.SysUserPostRepository;
import com.mmlm.useradmin.repository.SysUserRepository;
import com.mmlm.useradmin.repository.SysUserRoleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final SysUserRepository sysUserRepository;
    private final SysCompanyRepository sysCompanyRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysPostRepository sysPostRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final SysUserPostRepository sysUserPostRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(SysUserRepository sysUserRepository,
                       SysCompanyRepository sysCompanyRepository,
                       SysRoleRepository sysRoleRepository,
                       SysPostRepository sysPostRepository,
                       SysUserRoleRepository sysUserRoleRepository,
                       SysUserPostRepository sysUserPostRepository,
                       PasswordEncoder passwordEncoder) {
        this.sysUserRepository = sysUserRepository;
        this.sysCompanyRepository = sysCompanyRepository;
        this.sysRoleRepository = sysRoleRepository;
        this.sysPostRepository = sysPostRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.sysUserPostRepository = sysUserPostRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> list(String keyword, Integer status) {
        Specification<SysUser> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            if (StringUtils.hasText(keyword)) {
                String likeValue = "%" + keyword.trim() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("username"), likeValue),
                        criteriaBuilder.like(root.get("nickname"), likeValue),
                        criteriaBuilder.like(root.get("phone"), likeValue),
                        criteriaBuilder.like(root.get("email"), likeValue)
                ));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<SysUser> users = sysUserRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "id"));
        return buildUserResponses(users);
    }

    @Transactional
    public UserResponse create(UserSaveRequest request) {
        if (sysUserRepository.existsByUsername(request.getUsername().trim())) {
            throw new BusinessException("用户名已存在");
        }

        validateRelations(request.getCompanyId(), request.getRoleIds(), request.getPostIds());

        LocalDateTime now = LocalDateTime.now();
        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setNickname(request.getNickname().trim());
        user.setPassword(passwordEncoder.encode(StringUtils.hasText(request.getPassword()) ? request.getPassword() : "admin123"));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setAvatar(request.getAvatar());
        user.setCompanyId(request.getCompanyId());
        user.setStatus(request.getStatus());
        user.setRemark(request.getRemark());
        user.setTheme("light");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        sysUserRepository.save(user);

        replaceRelations(user.getId(), request.getRoleIds(), request.getPostIds());
        return buildUserResponses(Collections.singletonList(user)).get(0);
    }

    @Transactional
    public UserResponse update(Long id, UserSaveRequest request) {
        SysUser user = sysUserRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (sysUserRepository.existsByUsernameAndIdNot(request.getUsername().trim(), id)) {
            throw new BusinessException("用户名已存在");
        }

        validateRelations(request.getCompanyId(), request.getRoleIds(), request.getPostIds());

        user.setUsername(request.getUsername().trim());
        user.setNickname(request.getNickname().trim());
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setAvatar(request.getAvatar());
        user.setCompanyId(request.getCompanyId());
        user.setStatus(request.getStatus());
        user.setRemark(request.getRemark());
        user.setUpdatedAt(LocalDateTime.now());
        sysUserRepository.save(user);

        replaceRelations(user.getId(), request.getRoleIds(), request.getPostIds());
        return buildUserResponses(Collections.singletonList(user)).get(0);
    }

    @Transactional
    public void delete(Long id) {
        SysUser user = sysUserRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new BusinessException("内置管理员不允许删除");
        }

        sysUserRoleRepository.deleteByUserId(id);
        sysUserPostRepository.deleteByUserId(id);
        sysUserRepository.deleteById(id);
    }

    private void validateRelations(Long companyId, List<Long> roleIds, List<Long> postIds) {
        if (companyId != null && !sysCompanyRepository.existsById(companyId)) {
            throw new BusinessException("所属公司不存在");
        }
        if (!CollectionUtils.isEmpty(roleIds)) {
            List<SysRole> roles = sysRoleRepository.findAllById(roleIds);
            if (roles.size() != roleIds.size()) {
                throw new BusinessException("存在无效角色");
            }
        }
        if (!CollectionUtils.isEmpty(postIds)) {
            List<SysPost> posts = sysPostRepository.findAllById(postIds);
            if (posts.size() != postIds.size()) {
                throw new BusinessException("存在无效岗位");
            }
        }
    }

    private void replaceRelations(Long userId, List<Long> roleIds, List<Long> postIds) {
        sysUserRoleRepository.deleteByUserId(userId);
        sysUserPostRepository.deleteByUserId(userId);
        sysUserRoleRepository.flush();
        sysUserPostRepository.flush();

        if (!CollectionUtils.isEmpty(roleIds)) {
            List<SysUserRole> userRoles = roleIds.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(roleId -> new SysUserRole(userId, roleId))
                    .collect(Collectors.toList());
            sysUserRoleRepository.saveAll(userRoles);
        }

        if (!CollectionUtils.isEmpty(postIds)) {
            List<SysUserPost> userPosts = postIds.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(postId -> new SysUserPost(userId, postId))
                    .collect(Collectors.toList());
            sysUserPostRepository.saveAll(userPosts);
        }
    }

    private List<UserResponse> buildUserResponses(List<SysUser> users) {
        if (users.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());
        List<SysUserRole> userRoles = sysUserRoleRepository.findByUserIdIn(userIds);
        List<SysUserPost> userPosts = sysUserPostRepository.findByUserIdIn(userIds);

        Map<Long, List<Long>> userRoleIds = new LinkedHashMap<Long, List<Long>>();
        for (SysUserRole userRole : userRoles) {
            userRoleIds.computeIfAbsent(userRole.getUserId(), key -> new ArrayList<Long>()).add(userRole.getRoleId());
        }

        Map<Long, List<Long>> userPostIds = new LinkedHashMap<Long, List<Long>>();
        for (SysUserPost userPost : userPosts) {
            userPostIds.computeIfAbsent(userPost.getUserId(), key -> new ArrayList<Long>()).add(userPost.getPostId());
        }

        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).distinct().collect(Collectors.toList());
        List<Long> postIds = userPosts.stream().map(SysUserPost::getPostId).distinct().collect(Collectors.toList());
        List<Long> companyIds = users.stream()
                .map(SysUser::getCompanyId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> roleNameMap = roleIds.isEmpty()
                ? Collections.<Long, String>emptyMap()
                : sysRoleRepository.findAllById(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getName));
        Map<Long, String> postNameMap = postIds.isEmpty()
                ? Collections.<Long, String>emptyMap()
                : sysPostRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(SysPost::getId, SysPost::getName));
        Map<Long, String> companyNameMap = companyIds.isEmpty()
                ? Collections.<Long, String>emptyMap()
                : sysCompanyRepository.findAllById(companyIds).stream()
                .collect(Collectors.toMap(SysCompany::getId, SysCompany::getName));

        return users.stream()
                .sorted(Comparator.comparing(SysUser::getId).reversed())
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setId(user.getId());
                    response.setUsername(user.getUsername());
                    response.setNickname(user.getNickname());
                    response.setPhone(user.getPhone());
                    response.setEmail(user.getEmail());
                    response.setAvatar(user.getAvatar());
                    response.setCompanyId(user.getCompanyId());
                    response.setCompanyName(companyNameMap.get(user.getCompanyId()));
                    response.setStatus(user.getStatus());
                    response.setRemark(user.getRemark());
                    response.setCreatedAt(user.getCreatedAt());
                    response.setUpdatedAt(user.getUpdatedAt());

                    List<Long> currentRoleIds = userRoleIds.getOrDefault(user.getId(), Collections.<Long>emptyList());
                    List<Long> currentPostIds = userPostIds.getOrDefault(user.getId(), Collections.<Long>emptyList());

                    response.setRoleIds(currentRoleIds);
                    response.setPostIds(currentPostIds);
                    response.setRoleNames(currentRoleIds.stream()
                            .map(roleNameMap::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
                    response.setPostNames(currentPostIds.stream()
                            .map(postNameMap::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
                    return response;
                })
                .collect(Collectors.toList());
    }
}
