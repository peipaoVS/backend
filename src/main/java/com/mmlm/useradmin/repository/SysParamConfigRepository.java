package com.mmlm.useradmin.repository;

import com.mmlm.useradmin.entity.SysParamConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysParamConfigRepository extends JpaRepository<SysParamConfig, Long>, JpaSpecificationExecutor<SysParamConfig> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
