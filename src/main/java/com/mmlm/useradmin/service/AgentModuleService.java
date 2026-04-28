package com.mmlm.useradmin.service;

import com.mmlm.useradmin.common.AuthContext;
import com.mmlm.useradmin.common.BusinessException;
import com.mmlm.useradmin.dto.agent.AgentModuleResponse;
import com.mmlm.useradmin.dto.agent.AgentModuleSaveRequest;
import com.mmlm.useradmin.entity.SysAgentModule;
import com.mmlm.useradmin.entity.SysAgentModuleRole;
import com.mmlm.useradmin.entity.SysRole;
import com.mmlm.useradmin.repository.SysAgentModuleRepository;
import com.mmlm.useradmin.repository.SysAgentModuleRoleRepository;
import com.mmlm.useradmin.repository.SysRoleRepository;
import com.mmlm.useradmin.repository.SysUserRoleRepository;
import com.mmlm.useradmin.entity.SysUserRole;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AgentModuleService {

    private static final Map<String, String> PROVIDER_NAME_MAP = buildProviderNameMap();
    private static final Map<String, String> MODULE_TYPE_NAME_MAP = buildModuleTypeNameMap();
    private static final Map<String, List<String>> PROVIDER_TYPE_MAP = buildProviderTypeMap();

    private final SysAgentModuleRepository sysAgentModuleRepository;
    private final SysAgentModuleRoleRepository sysAgentModuleRoleRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;

    public AgentModuleService(SysAgentModuleRepository sysAgentModuleRepository,
                              SysAgentModuleRoleRepository sysAgentModuleRoleRepository,
                              SysRoleRepository sysRoleRepository,
                              SysUserRoleRepository sysUserRoleRepository) {
        this.sysAgentModuleRepository = sysAgentModuleRepository;
        this.sysAgentModuleRoleRepository = sysAgentModuleRoleRepository;
        this.sysRoleRepository = sysRoleRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
    }

    public List<AgentModuleResponse> list(String keyword, String moduleType) {
        Specification<SysAgentModule> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            if (StringUtils.hasText(keyword)) {
                String likeValue = "%" + keyword.trim() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("name"), likeValue),
                        criteriaBuilder.like(root.get("baseModel"), likeValue),
                        criteriaBuilder.like(root.get("providerCode"), likeValue)
                ));
            }
            if (StringUtils.hasText(moduleType)) {
                predicates.add(criteriaBuilder.equal(root.get("moduleType"), moduleType.trim()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<SysAgentModule> modules = sysAgentModuleRepository.findAll(
                specification,
                Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );
        return buildResponses(modules);
    }

    public List<AgentModuleResponse> listAvailableForCurrentUser(String moduleType) {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            return Collections.emptyList();
        }

        List<Long> roleIds = sysUserRoleRepository.findByUserId(userId).stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> moduleIds = sysAgentModuleRoleRepository.findByRoleIdIn(roleIds).stream()
                .map(SysAgentModuleRole::getAgentModuleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (moduleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<SysAgentModule> modules = sysAgentModuleRepository.findAllById(moduleIds).stream()
                .filter(module -> !StringUtils.hasText(moduleType)
                        || moduleType.trim().equals(module.getModuleType()))
                .collect(Collectors.toList());

        return buildResponses(modules).stream()
                .peek(item -> item.setApiKey(null))
                .collect(Collectors.toList());
    }

    @Transactional
    public AgentModuleResponse create(AgentModuleSaveRequest request) {
        validateRequest(request, null);

        LocalDateTime now = LocalDateTime.now();
        String operator = resolveOperator();

        SysAgentModule module = new SysAgentModule();
        module.setName(request.getName().trim());
        module.setProviderCode(request.getProviderCode().trim());
        module.setModuleType(request.getModuleType().trim());
        module.setBaseModel(request.getBaseModel().trim());
        module.setApiDomain(request.getApiDomain().trim());
        module.setApiKey(request.getApiKey().trim());
        module.setRemark(normalizeRemark(request.getRemark()));
        module.setCreatedBy(operator);
        module.setUpdatedBy(operator);
        module.setCreatedAt(now);
        module.setUpdatedAt(now);
        sysAgentModuleRepository.save(module);

        replaceRoleRelations(module.getId(), request.getRoleIds());
        return buildResponses(Collections.singletonList(module)).get(0);
    }

    @Transactional
    public AgentModuleResponse update(Long id, AgentModuleSaveRequest request) {
        SysAgentModule module = sysAgentModuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("智能体模块不存在"));
        validateRequest(request, id);

        module.setName(request.getName().trim());
        module.setProviderCode(request.getProviderCode().trim());
        module.setModuleType(request.getModuleType().trim());
        module.setBaseModel(request.getBaseModel().trim());
        module.setApiDomain(request.getApiDomain().trim());
        module.setApiKey(request.getApiKey().trim());
        module.setRemark(normalizeRemark(request.getRemark()));
        module.setUpdatedBy(resolveOperator());
        module.setUpdatedAt(LocalDateTime.now());
        sysAgentModuleRepository.save(module);

        replaceRoleRelations(id, request.getRoleIds());
        return buildResponses(Collections.singletonList(module)).get(0);
    }

    @Transactional
    public void delete(Long id) {
        SysAgentModule module = sysAgentModuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("智能体模块不存在"));
        sysAgentModuleRoleRepository.deleteByAgentModuleId(id);
        sysAgentModuleRoleRepository.flush();
        sysAgentModuleRepository.delete(module);
    }

    private void validateRequest(AgentModuleSaveRequest request, Long id) {
        String name = request.getName().trim();
        String providerCode = request.getProviderCode().trim();
        String moduleType = request.getModuleType().trim();

        if (!PROVIDER_NAME_MAP.containsKey(providerCode)) {
            throw new BusinessException("不支持的供应商类型");
        }
        if (!MODULE_TYPE_NAME_MAP.containsKey(moduleType)) {
            throw new BusinessException("不支持的模块类型");
        }
        if (!PROVIDER_TYPE_MAP.get(providerCode).contains(moduleType)) {
            throw new BusinessException("当前供应商不支持所选模块类型");
        }

        if (id == null) {
            if (sysAgentModuleRepository.existsByName(name)) {
                throw new BusinessException("模块名称已存在");
            }
        } else if (sysAgentModuleRepository.existsByNameAndIdNot(name, id)) {
            throw new BusinessException("模块名称已存在");
        }

        validateRoleIds(request.getRoleIds());
    }

    private void validateRoleIds(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            throw new BusinessException("请至少绑定一个角色");
        }

        List<Long> distinctRoleIds = roleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (distinctRoleIds.isEmpty()) {
            throw new BusinessException("请至少绑定一个角色");
        }
        List<SysRole> roles = sysRoleRepository.findAllById(distinctRoleIds);
        if (roles.size() != distinctRoleIds.size()) {
            throw new BusinessException("存在无效角色");
        }
    }

    private void replaceRoleRelations(Long agentModuleId, List<Long> roleIds) {
        sysAgentModuleRoleRepository.deleteByAgentModuleId(agentModuleId);
        sysAgentModuleRoleRepository.flush();

        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        List<SysAgentModuleRole> relations = roleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(roleId -> new SysAgentModuleRole(agentModuleId, roleId))
                .collect(Collectors.toList());
        sysAgentModuleRoleRepository.saveAll(relations);
    }

    private List<AgentModuleResponse> buildResponses(List<SysAgentModule> modules) {
        if (modules.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> moduleIds = modules.stream().map(SysAgentModule::getId).collect(Collectors.toList());
        List<SysAgentModuleRole> relations = sysAgentModuleRoleRepository.findByAgentModuleIdIn(moduleIds);

        Map<Long, List<Long>> moduleRoleIds = new LinkedHashMap<Long, List<Long>>();
        for (SysAgentModuleRole relation : relations) {
            moduleRoleIds.computeIfAbsent(relation.getAgentModuleId(), key -> new ArrayList<Long>())
                    .add(relation.getRoleId());
        }

        List<Long> roleIds = relations.stream()
                .map(SysAgentModuleRole::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> roleNameMap = roleIds.isEmpty()
                ? Collections.<Long, String>emptyMap()
                : sysRoleRepository.findAllById(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getName));

        return modules.stream()
                .sorted(Comparator.comparing(SysAgentModule::getUpdatedAt).reversed()
                        .thenComparing(SysAgentModule::getId, Comparator.reverseOrder()))
                .map(module -> {
                    List<Long> currentRoleIds = moduleRoleIds.getOrDefault(module.getId(), Collections.<Long>emptyList());

                    AgentModuleResponse response = new AgentModuleResponse();
                    response.setId(module.getId());
                    response.setName(module.getName());
                    response.setProviderCode(module.getProviderCode());
                    response.setProviderName(PROVIDER_NAME_MAP.get(module.getProviderCode()));
                    response.setModuleType(module.getModuleType());
                    response.setModuleTypeName(MODULE_TYPE_NAME_MAP.get(module.getModuleType()));
                    response.setBaseModel(module.getBaseModel());
                    response.setApiDomain(module.getApiDomain());
                    response.setApiKey(module.getApiKey());
                    response.setRemark(module.getRemark());
                    response.setCreatedBy(module.getCreatedBy());
                    response.setUpdatedBy(module.getUpdatedBy());
                    response.setRoleIds(currentRoleIds);
                    response.setRoleNames(currentRoleIds.stream()
                            .map(roleNameMap::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
                    response.setCreatedAt(module.getCreatedAt());
                    response.setUpdatedAt(module.getUpdatedAt());
                    return response;
                })
                .collect(Collectors.toList());
    }

    private String resolveOperator() {
        String username = AuthContext.getUsername();
        return StringUtils.hasText(username) ? username : "system";
    }

    private String normalizeRemark(String remark) {
        return StringUtils.hasText(remark) ? remark.trim() : null;
    }

    private static Map<String, String> buildProviderNameMap() {
        Map<String, String> providerNameMap = new LinkedHashMap<String, String>();
        providerNameMap.put("deepseek", "DeepSeek");
        providerNameMap.put("ollama", "Ollama");
        providerNameMap.put("openai", "OpenAI");
        providerNameMap.put("tongyi", "通义千问");
        providerNameMap.put("qianfan", "千帆大模型");
        providerNameMap.put("zhipu", "智谱 AI");
        return providerNameMap;
    }

    private static Map<String, String> buildModuleTypeNameMap() {
        Map<String, String> moduleTypeNameMap = new LinkedHashMap<String, String>();
        moduleTypeNameMap.put("language", "语言模型");
        moduleTypeNameMap.put("embedding", "向量模型");
        return moduleTypeNameMap;
    }

    private static Map<String, List<String>> buildProviderTypeMap() {
        Map<String, List<String>> providerTypeMap = new LinkedHashMap<String, List<String>>();
        providerTypeMap.put("deepseek", Collections.singletonList("language"));
        providerTypeMap.put("ollama", buildTypes("language", "embedding"));
        providerTypeMap.put("openai", buildTypes("language", "embedding"));
        providerTypeMap.put("tongyi", buildTypes("language", "embedding"));
        providerTypeMap.put("qianfan", buildTypes("language", "embedding"));
        providerTypeMap.put("zhipu", buildTypes("language", "embedding"));
        return providerTypeMap;
    }

    private static List<String> buildTypes(String... types) {
        List<String> values = new ArrayList<String>();
        Collections.addAll(values, types);
        return values;
    }
}
