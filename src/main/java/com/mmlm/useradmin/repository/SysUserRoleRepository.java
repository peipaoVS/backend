package com.mmlm.useradmin.repository;

import com.mmlm.useradmin.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long> {

    List<SysUserRole> findByUserIdIn(List<Long> userIds);

    List<SysUserRole> findByRoleIdIn(List<Long> roleIds);

    List<SysUserRole> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
