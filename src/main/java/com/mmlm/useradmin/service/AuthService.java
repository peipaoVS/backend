package com.mmlm.useradmin.service;

import com.mmlm.useradmin.auth.TokenSessionService;
import com.mmlm.useradmin.common.AuthContext;
import com.mmlm.useradmin.common.BusinessException;
import com.mmlm.useradmin.dto.auth.LoginRequest;
import com.mmlm.useradmin.dto.auth.LoginResponse;
import com.mmlm.useradmin.dto.auth.ThemeUpdateRequest;
import com.mmlm.useradmin.dto.auth.UserMenuResponse;
import com.mmlm.useradmin.dto.auth.UserProfileResponse;
import com.mmlm.useradmin.entity.SysCompany;
import com.mmlm.useradmin.entity.SysMenu;
import com.mmlm.useradmin.entity.SysPost;
import com.mmlm.useradmin.entity.SysRole;
import com.mmlm.useradmin.entity.SysRoleMenu;
import com.mmlm.useradmin.entity.SysUser;
import com.mmlm.useradmin.entity.SysUserPost;
import com.mmlm.useradmin.entity.SysUserRole;
import com.mmlm.useradmin.repository.SysCompanyRepository;
import com.mmlm.useradmin.repository.SysMenuRepository;
import com.mmlm.useradmin.repository.SysPostRepository;
import com.mmlm.useradmin.repository.SysRoleMenuRepository;
import com.mmlm.useradmin.repository.SysRoleRepository;
import com.mmlm.useradmin.repository.SysUserPostRepository;
import com.mmlm.useradmin.repository.SysUserRepository;
import com.mmlm.useradmin.repository.SysUserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private static final String THEME_LIGHT = "light";
    private static final String THEME_DARK = "dark";

    private final SysUserRepository sysUserRepository;
    private final SysCompanyRepository sysCompanyRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final SysUserPostRepository sysUserPostRepository;
    private final SysRoleMenuRepository sysRoleMenuRepository;
    private final SysMenuRepository sysMenuRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysPostRepository sysPostRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenSessionService tokenSessionService;

    public AuthService(SysUserRepository sysUserRepository,
                       SysCompanyRepository sysCompanyRepository,
                       SysUserRoleRepository sysUserRoleRepository,
                       SysUserPostRepository sysUserPostRepository,
                       SysRoleMenuRepository sysRoleMenuRepository,
                       SysMenuRepository sysMenuRepository,
                       SysRoleRepository sysRoleRepository,
                       SysPostRepository sysPostRepository,
                       PasswordEncoder passwordEncoder,
                       TokenSessionService tokenSessionService) {
        this.sysUserRepository = sysUserRepository;
        this.sysCompanyRepository = sysCompanyRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.sysUserPostRepository = sysUserPostRepository;
        this.sysRoleMenuRepository = sysRoleMenuRepository;
        this.sysMenuRepository = sysMenuRepository;
        this.sysRoleRepository = sysRoleRepository;
        this.sysPostRepository = sysPostRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenSessionService = tokenSessionService;
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Invalid username or password"));

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("Current user is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid username or password");
        }

        String token = tokenSessionService.create(user.getId(), user.getUsername());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(buildProfile(user));
        return response;
    }

    public UserProfileResponse me() {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            throw new BusinessException("Not logged in");
        }
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        return buildProfile(user);
    }

    public void logout(String token) {
        tokenSessionService.remove(token);
    }

    @Transactional
    public UserProfileResponse updateTheme(ThemeUpdateRequest request) {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            throw new BusinessException("Not logged in");
        }

        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        user.setTheme(normalizeTheme(request.getTheme()));
        sysUserRepository.save(user);
        return buildProfile(user);
    }

    private UserProfileResponse buildProfile(SysUser user) {
        List<SysUserRole> userRoles = sysUserRoleRepository.findByUserId(user.getId());
        List<SysUserPost> userPosts = sysUserPostRepository.findByUserId(user.getId());

        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<Long> postIds = userPosts.stream().map(SysUserPost::getPostId).collect(Collectors.toList());

        Map<Long, SysRole> roleMap = roleIds.isEmpty()
                ? Collections.<Long, SysRole>emptyMap()
                : sysRoleRepository.findAllById(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, role -> role));
        Map<Long, String> postNameMap = postIds.isEmpty()
                ? Collections.<Long, String>emptyMap()
                : sysPostRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(SysPost::getId, SysPost::getName));

        SysCompany company = null;
        if (user.getCompanyId() != null) {
            company = sysCompanyRepository.findById(user.getCompanyId()).orElse(null);
        }

        boolean adminUser = userRoles.stream()
                .map(item -> roleMap.get(item.getRoleId()))
                .filter(Objects::nonNull)
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getCode()));

        List<SysMenu> visibleMenus;
        if (adminUser) {
            visibleMenus = sysMenuRepository.findAll().stream()
                    .filter(menu -> Integer.valueOf(1).equals(menu.getStatus()))
                    .collect(Collectors.toList());
        } else if (roleIds.isEmpty()) {
            visibleMenus = Collections.emptyList();
        } else {
            List<Long> menuIds = sysRoleMenuRepository.findByRoleIdIn(roleIds).stream()
                    .map(SysRoleMenu::getMenuId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            visibleMenus = menuIds.isEmpty()
                    ? Collections.<SysMenu>emptyList()
                    : sysMenuRepository.findAllById(menuIds).stream()
                    .filter(menu -> Integer.valueOf(1).equals(menu.getStatus()))
                    .collect(Collectors.toList());
        }

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setCompanyId(user.getCompanyId());
        response.setCompanyName(company == null ? null : company.getName());
        response.setTheme(normalizeTheme(user.getTheme()));
        response.setRoleNames(userRoles.stream()
                .map(item -> roleMap.get(item.getRoleId()))
                .filter(Objects::nonNull)
                .map(SysRole::getName)
                .collect(Collectors.toList()));
        response.setPostNames(userPosts.stream()
                .map(item -> postNameMap.get(item.getPostId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setMenus(visibleMenus.stream()
                .sorted(Comparator.comparing(SysMenu::getSection)
                        .thenComparing(SysMenu::getSortOrder)
                        .thenComparing(SysMenu::getId))
                .map(menu -> {
                    UserMenuResponse item = new UserMenuResponse();
                    item.setName(menu.getName());
                    item.setCode(menu.getCode());
                    item.setSection(menu.getSection());
                    item.setPath(menu.getPath());
                    item.setSortOrder(menu.getSortOrder());
                    return item;
                })
                .collect(Collectors.toList()));
        return response;
    }

    private String normalizeTheme(String value) {
        String theme = String.valueOf(value == null ? "" : value).trim().toLowerCase();
        if (THEME_DARK.equals(theme)) {
            return THEME_DARK;
        }
        return THEME_LIGHT;
    }
}
