package com.mmlm.useradmin.repository;

import com.mmlm.useradmin.entity.SysCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysCompanyRepository extends JpaRepository<SysCompany, Long>, JpaSpecificationExecutor<SysCompany> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
