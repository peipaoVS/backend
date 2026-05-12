package com.mmlm.useradmin.repository;

import com.mmlm.useradmin.entity.SysSessionCode;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SysSessionCodeRepository extends CrudRepository<SysSessionCode, Long>, JpaSpecificationExecutor<SysSessionCode> {

    List<SysSessionCode> findByUserId(Long userId);

    List<SysSessionCode> findByPostId(Long postId);

    List<SysSessionCode> findByPostIdAndIsActive(Long postId, Integer isActive);

    Optional<SysSessionCode> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT s FROM SysSessionCode s WHERE s.postId = :postId AND s.isActive = 1 ORDER BY s.createdAt DESC")
    List<SysSessionCode> findActiveByPostId(@Param("postId") Long postId);
}
