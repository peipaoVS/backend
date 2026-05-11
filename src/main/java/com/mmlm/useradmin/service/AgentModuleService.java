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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate;

    public AgentModuleService(SysAgentModuleRepository sysAgentModuleRepository,
                              SysAgentModuleRoleRepository sysAgentModuleRoleRepository,
                              SysRoleRepository sysRoleRepository,
                              SysUserRoleRepository sysUserRoleRepository,
                              RestTemplate restTemplate) {
        this.sysAgentModuleRepository = sysAgentModuleRepository;
        this.sysAgentModuleRoleRepository = sysAgentModuleRoleRepository;
        this.sysRoleRepository = sysRoleRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.restTemplate = restTemplate;
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

    public Object chat(Map<String, Object> request) {
        Object modelIdObj = request.get("modelId");
        if (modelIdObj == null) {
            throw new BusinessException("modelId 不能为空");
        }
        Long modelId = modelIdObj instanceof Number ? ((Number) modelIdObj).longValue() : Long.parseLong(modelIdObj.toString());
        SysAgentModule module = sysAgentModuleRepository.findById(modelId)
                .orElseThrow(() -> new BusinessException("智能体模块不存在"));

        String domain = module.getApiDomain().trim();
        while (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }

        String endpointPath = (String) request.get("endpoint");
        String url;
        if (endpointPath != null) {
            url = domain + endpointPath;
        } else if (domain.matches(".+/v\\d+$")) {
            url = domain + "/chat/completions";
        } else {
            url = domain + "/v1/chat/completions";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(module.getApiKey())) {
            headers.setBearerAuth(module.getApiKey());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        if (endpointPath != null) {
            body.putAll(request);
        } else {
            body.put("model", module.getBaseModel());
            body.put("messages", request.get("messages"));
        }
        body.remove("modelId");
        body.remove("endpoint");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            if (endpointPath != null) {
                String raw = restTemplate.postForObject(url, entity, String.class);
                String answer = parseRuleQaResponse(raw);
                if (answer != null) {
                    return Collections.singletonMap("answer", answer);
                }
                return Collections.singletonMap("raw", raw);
            }
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return response.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            throw new BusinessException(responseBody);
        } catch (Exception e) {
            throw new BusinessException("请求模型 API 失败: " + e.getMessage());
        }
    }

    private String parseRuleQaResponse(String sse) {
        if (sse == null) return null;
        StringBuilder deltaBuilder = new StringBuilder();
        for (String line : sse.split("\n")) {
            if (line.startsWith("data: ")) {
                String jsonStr = line.substring(6).trim();
                if (jsonStr.isEmpty() || jsonStr.equals("[DONE]")) continue;
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, Object> event = mapper.readValue(jsonStr, Map.class);
                    if ("TEXT_MESSAGE_CONTENT".equals(event.get("type")) && event.containsKey("delta")) {
                        deltaBuilder.append(event.get("delta"));
                    }
                } catch (Exception ignored) {
                }
            }
        }
        if (deltaBuilder.length() == 0) return null;
        String combined = deltaBuilder.toString();
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> data = mapper.readValue(combined, Map.class);
            if (data.containsKey("answer_text")) return (String) data.get("answer_text");
            if (data.containsKey("raw_final_answer")) return (String) data.get("raw_final_answer");
        } catch (Exception ignored) {
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"answer_text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(combined);
        if (m.find()) {
            String raw = m.group(1);
            return raw.replace("\\n", "\n").replace("\\t", "\t").replace("\\r", "\r");
        }
        return combined;
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
