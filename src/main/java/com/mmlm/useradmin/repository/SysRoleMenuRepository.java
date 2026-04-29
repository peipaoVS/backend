package com.mmlm.useradmin.repository;

import com.mmlm.useradmin.entity.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, Long> {

    List<SysRoleMenu> findByRoleIdIn(List<Long> roleIds);

    List<SysRoleMenu> findByMenuIdIn(List<Long> menuIds);

    void deleteByRoleId(Long roleId);

    void deleteByMenuId(Long menuId);
}
