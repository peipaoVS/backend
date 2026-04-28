package com.mmlm.useradmin.repository;

import com.mmlm.useradmin.entity.SysPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysPostRepository extends JpaRepository<SysPost, Long>, JpaSpecificationExecutor<SysPost> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
