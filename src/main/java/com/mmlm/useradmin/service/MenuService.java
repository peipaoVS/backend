package com.mmlm.useradmin.service;

import com.mmlm.useradmin.common.BusinessException;
import com.mmlm.useradmin.dto.menu.MenuResponse;
import com.mmlm.useradmin.dto.menu.MenuSaveRequest;
import com.mmlm.useradmin.entity.SysMenu;
import com.mmlm.useradmin.entity.SysRole;
import com.mmlm.useradmin.entity.SysRoleMenu;
import com.mmlm.useradmin.repository.SysMenuRepository;
import com.mmlm.useradmin.repository.SysRoleMenuRepository;
import com.mmlm.useradmin.repository.SysRoleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
public class MenuService {

    private final SysMenuRepository sysMenuRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysRoleMenuRepository sysRoleMenuRepository;

    public MenuService(SysMenuRepository sysMenuRepository,
                       SysRoleRepository sysRoleRepository,
                       SysRoleMenuRepository sysRoleMenuRepository) {
        this.sysMenuRepository = sysMenuRepository;
        this.sysRoleRepository = sysRoleRepository;
        this.sysRoleMenuRepository = sysRoleMenuRepository;
    }

    public List<MenuResponse> list(String keyword, Integer status) {
        Specification<SysMenu> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            if (StringUtils.hasText(keyword)) {
                String likeValue = "%" + keyword.trim() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("name"), likeValue),
                        criteriaBuilder.like(root.get("code"), likeValue),
                        criteriaBuilder.like(root.get("path"), likeValue)
                ));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        List<SysMenu> menus = sysMenuRepository.findAll(
                specification,
                Sort.by(Sort.Direction.ASC, "section")
                        .and(Sort.by(Sort.Direction.ASC, "sortOrder"))
                        .and(Sort.by(Sort.Direction.ASC, "id"))
        );
        return buildResponses(menus);
    }

    @Transactional
    public MenuResponse create(MenuSaveRequest request) {
        validateRequest(request, null);

        LocalDateTime now = LocalDateTime.now();
        SysMenu menu = new SysMenu();
        menu.setName(request.getName().trim());
        menu.setCode(request.getCode().trim());
        menu.setSection(request.getSection().trim());
        menu.setPath(request.getPath().trim());
        menu.setSortOrder(request.getSortOrder());
        menu.setStatus(request.getStatus());
        menu.setRemark(request.getRemark());
        menu.setCreatedAt(now);
        menu.setUpdatedAt(now);
        sysMenuRepository.save(menu);

        replaceRoleRelations(menu.getId(), request.getRoleIds());
        return buildResponses(Collections.singletonList(menu)).get(0);
    }

    @Transactional
    public MenuResponse update(Long id, MenuSaveRequest request) {
        SysMenu menu = sysMenuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在"));
        validateRequest(request, id);

        menu.setName(request.getName().trim());
        menu.setCode(request.getCode().trim());
        menu.setSection(request.getSection().trim());
        menu.setPath(request.getPath().trim());
        menu.setSortOrder(request.getSortOrder());
        menu.setStatus(request.getStatus());
        menu.setRemark(request.getRemark());
        menu.setUpdatedAt(LocalDateTime.now());
        sysMenuRepository.save(menu);

        replaceRoleRelations(menu.getId(), request.getRoleIds());
        return buildResponses(Collections.singletonList(menu)).get(0);
    }

    @Transactional
    public void delete(Long id) {
        SysMenu menu = sysMenuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在"));
        sysRoleMenuRepository.deleteByMenuId(id);
        sysRoleMenuRepository.flush();
        sysMenuRepository.delete(menu);
    }

    private void validateRequest(MenuSaveRequest request, Long id) {
        String code = request.getCode().trim();
        String path = request.getPath().trim();
        if (id == null) {
            if (sysMenuRepository.existsByCode(code)) {
                throw new BusinessException("菜单编码已存在");
            }
            if (sysMenuRepository.existsByPath(path)) {
                throw new BusinessException("菜单路径已存在");
            }
        } else {
            if (sysMenuRepository.existsByCodeAndIdNot(code, id)) {
                throw new BusinessException("菜单编码已存在");
            }
            if (sysMenuRepository.existsByPathAndIdNot(path, id)) {
                throw new BusinessException("菜单路径已存在");
            }
        }

        if (!CollectionUtils.isEmpty(request.getRoleIds())) {
            List<Long> roleIds = request.getRoleIds().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            List<SysRole> roles = sysRoleRepository.findAllById(roleIds);
            if (roles.size() != roleIds.size()) {
                throw new BusinessException("存在无效角色");
            }
        }
    }

    private void replaceRoleRelations(Long menuId, List<Long> roleIds) {
        sysRoleMenuRepository.deleteByMenuId(menuId);
        sysRoleMenuRepository.flush();

        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        List<SysRoleMenu> roleMenus = roleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(roleId -> new SysRoleMenu(roleId, menuId))
                .collect(Collectors.toList());
        sysRoleMenuRepository.saveAll(roleMenus);
    }

    private List<MenuResponse> buildResponses(List<SysMenu> menus) {
        if (menus.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> menuIds = menus.stream().map(SysMenu::getId).collect(Collectors.toList());
        List<SysRoleMenu> roleMenus = sysRoleMenuRepository.findByMenuIdIn(menuIds);
        Map<Long, List<Long>> menuRoleIds = new LinkedHashMap<Long, List<Long>>();
        for (SysRoleMenu roleMenu : roleMenus) {
            menuRoleIds.computeIfAbsent(roleMenu.getMenuId(), key -> new ArrayList<Long>()).add(roleMenu.getRoleId());
        }

        List<Long> roleIds = roleMenus.stream().map(SysRoleMenu::getRoleId).distinct().collect(Collectors.toList());
        Map<Long, String> roleNameMap = roleIds.isEmpty()
                ? Collections.<Long, String>emptyMap()
                : sysRoleRepository.findAllById(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getName));

        return menus.stream()
                .sorted(Comparator.comparing(SysMenu::getSection)
                        .thenComparing(SysMenu::getSortOrder)
                        .thenComparing(SysMenu::getId))
                .map(menu -> {
                    List<Long> currentRoleIds = menuRoleIds.getOrDefault(menu.getId(), Collections.<Long>emptyList());

                    MenuResponse response = new MenuResponse();
                    response.setId(menu.getId());
                    response.setName(menu.getName());
                    response.setCode(menu.getCode());
                    response.setSection(menu.getSection());
                    response.setPath(menu.getPath());
                    response.setSortOrder(menu.getSortOrder());
                    response.setStatus(menu.getStatus());
                    response.setRemark(menu.getRemark());
                    response.setRoleIds(currentRoleIds);
                    response.setRoleNames(currentRoleIds.stream()
                            .map(roleNameMap::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
                    response.setCreatedAt(menu.getCreatedAt());
                    response.setUpdatedAt(menu.getUpdatedAt());
                    return response;
                })
                .collect(Collectors.toList());
    }
}
