package com.mmlm.useradmin.repository;

import com.mmlm.useradmin.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysRoleRepository extends JpaRepository<SysRole, Long>, JpaSpecificationExecutor<SysRole> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
