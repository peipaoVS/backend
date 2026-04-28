package com.mmlm.useradmin.repository;

import com.mmlm.useradmin.entity.SysAgentModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysAgentModuleRepository extends JpaRepository<SysAgentModule, Long>, JpaSpecificationExecutor<SysAgentModule> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
