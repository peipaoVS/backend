package com.mmlm.useradmin.repository;

import com.mmlm.useradmin.entity.SysUserPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysUserPostRepository extends JpaRepository<SysUserPost, Long> {

    List<SysUserPost> findByUserIdIn(List<Long> userIds);

    List<SysUserPost> findByPostIdIn(List<Long> postIds);

    List<SysUserPost> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
