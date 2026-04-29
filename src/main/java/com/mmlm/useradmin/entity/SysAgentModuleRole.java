package com.mmlm.useradmin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sys_agent_module_role")
public class SysAgentModuleRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_module_id", nullable = false)
    private Long agentModuleId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    public SysAgentModuleRole() {
    }

    public SysAgentModuleRole(Long agentModuleId, Long roleId) {
        this.agentModuleId = agentModuleId;
        this.roleId = roleId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAgentModuleId() {
        return agentModuleId;
    }

    public void setAgentModuleId(Long agentModuleId) {
        this.agentModuleId = agentModuleId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
