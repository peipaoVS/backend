package com.mmlm.useradmin.repository;

import com.mmlm.useradmin.entity.SysAgentModuleRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysAgentModuleRoleRepository extends JpaRepository<SysAgentModuleRole, Long> {

    List<SysAgentModuleRole> findByAgentModuleIdIn(List<Long> agentModuleIds);

    List<SysAgentModuleRole> findByRoleIdIn(List<Long> roleIds);

    long countByRoleId(Long roleId);

    void deleteByAgentModuleId(Long agentModuleId);
}
