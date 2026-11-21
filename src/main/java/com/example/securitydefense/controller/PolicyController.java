package com.example.securitydefense.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.securitydefense.common.Result;
import com.example.securitydefense.entity.FirewallRule;
import com.example.securitydefense.entity.SecurityPolicy;
import com.example.securitydefense.entity.PolicyTemplate;
import com.example.securitydefense.mapper.FirewallRuleMapper;
import com.example.securitydefense.mapper.SecurityPolicyMapper;
import com.example.securitydefense.mapper.PolicyTemplateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 策略管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/policy")
public class PolicyController {

    @Autowired
    private FirewallRuleMapper firewallRuleMapper;

    @Autowired
    private SecurityPolicyMapper securityPolicyMapper;

    @Autowired
    private PolicyTemplateMapper policyTemplateMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取防火墙规则列表
     */
    @GetMapping("/firewall-rules")
    public Result<Map<String, Object>> getFirewallRules(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取防火墙规则列表，页码: {}, 每页数量: {}", page, pageSize);
        
        // 使用MyBatis Plus分页查询
        Page<FirewallRule> firewallRulePage = new Page<>((long) page, pageSize);
        IPage<FirewallRule> resultPage = firewallRuleMapper.selectPage(firewallRulePage, null);
        
        // 转换为Map格式
        List<Map<String, Object>> records = new ArrayList<>();
        for (FirewallRule rule : resultPage.getRecords()) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", rule.getId());
            record.put("name", rule.getRuleName());
            record.put("sourceIp", rule.getSourceIp());
            record.put("targetIp", rule.getTargetIp());
            record.put("protocol", rule.getProtocol());
            record.put("port", rule.getTargetPort() != null ? rule.getTargetPort() : "");
            record.put("action", rule.getStatus() != null && rule.getStatus() == 1 ? "允许" : "拒绝");
            record.put("enabled", rule.getStatus() != null && rule.getStatus() == 1);
            record.put("priority", rule.getPriority());
            record.put("description", rule.getDescription());
            record.put("createTime", rule.getCreateTime() != null ? rule.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");
            records.add(record);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", records);
        result.put("total", resultPage.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return Result.success(result);
    }

    /**
     * 添加防火墙规则
     */
    @PostMapping("/firewall-rules")
    public Result<Map<String, Object>> addFirewallRule(@RequestBody Map<String, Object> rule) {
        log.info("添加防火墙规则: {}", rule);
        rule.put("id", System.currentTimeMillis());
        rule.put("createTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return Result.success(rule);
    }

    /**
     * 更新防火墙规则
     */
    @PutMapping("/firewall-rules/{id}")
    public Result<Void> updateFirewallRule(@PathVariable Long id, @RequestBody Map<String, Object> rule) {
        log.info("更新防火墙规则，ID: {}, 数据: {}", id, rule);
        return Result.success(null);
    }

    /**
     * 删除防火墙规则
     */
    @DeleteMapping("/firewall-rules/{id}")
    public Result<Void> deleteFirewallRule(@PathVariable Long id) {
        log.info("删除防火墙规则，ID: {}", id);
        try {
            // 执行数据库删除操作
            int deletedCount = firewallRuleMapper.deleteById(id);
            if (deletedCount > 0) {
                return Result.success(null);
            } else {
                return Result.error("未找到要删除的规则");
            }
        } catch (Exception e) {
            log.error("删除防火墙规则失败，ID: {}", id, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 切换防火墙规则启用状态
     */
    @PutMapping("/firewall-rules/{id}/toggle")
    public Result<Void> toggleFirewallRule(@PathVariable Long id, @RequestBody Map<String, Boolean> params) {
        log.info("切换防火墙规则状态，ID: {}, 启用: {}", id, params.get("enabled"));
        return Result.success(null);
    }

    /**
     * 获取安全策略列表
     */
    @GetMapping("/security-policies")
    public Result<Map<String, Object>> getSecurityPolicies(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取安全策略列表，页码: {}, 每页数量: {}", page, pageSize);
        
        // 使用MyBatis Plus分页查询
        Page<SecurityPolicy> securityPolicyPage = new Page<>((long) page, pageSize);
        IPage<SecurityPolicy> resultPage = securityPolicyMapper.selectPage(securityPolicyPage, null);
        
        // 转换为Map格式
        List<Map<String, Object>> records = new ArrayList<>();
        for (SecurityPolicy policy : resultPage.getRecords()) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", policy.getId());
            record.put("name", policy.getPolicyName());
            record.put("type", policy.getPolicyType());
            record.put("level", policy.getEnabled() != null && policy.getEnabled() == 1 ? "高" : "低");
            record.put("enabled", policy.getEnabled() != null && policy.getEnabled() == 1);
            record.put("scope", "全局");
            record.put("description", policy.getDescription());
            record.put("createTime", policy.getCreateTime() != null ? policy.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");
            records.add(record);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", records);
        result.put("total", resultPage.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return Result.success(result);
    }

    /**
     * 添加安全策略
     */
    @PostMapping("/security-policies")
    public Result<Map<String, Object>> addSecurityPolicy(@RequestBody Map<String, Object> policy) {
        log.info("添加安全策略: {}", policy);
        policy.put("id", System.currentTimeMillis());
        policy.put("createTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return Result.success(policy);
    }

    /**
     * 更新安全策略
     */
    @PutMapping("/security-policies/{id}")
    public Result<Void> updateSecurityPolicy(@PathVariable Long id, @RequestBody Map<String, Object> policy) {
        log.info("更新安全策略，ID: {}, 数据: {}", id, policy);
        return Result.success(null);
    }

    /**
     * 删除安全策略
     */
    @DeleteMapping("/security-policies/{id}")
    public Result<Void> deleteSecurityPolicy(@PathVariable Long id) {
        log.info("删除安全策略，ID: {}", id);
        return Result.success(null);
    }

    /**
     * 获取策略模板
     */
    @GetMapping("/templates")
    public Result<List<Map<String, Object>>> getPolicyTemplates() {
        log.info("获取策略模板");

        // 查询策略模板表（只查询未删除的）
        LambdaQueryWrapper<PolicyTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PolicyTemplate::getDeleted, 0);
        List<PolicyTemplate> templatesFromDb = policyTemplateMapper.selectList(queryWrapper);

        // 转换为Map格式
        List<Map<String, Object>> templates = new ArrayList<>();
        for (PolicyTemplate template : templatesFromDb) {
            Map<String, Object> templateMap = new HashMap<>();
            templateMap.put("id", template.getId());
            templateMap.put("name", template.getTemplateName());
            templateMap.put("description", template.getDescription());
            // 将英文类型转换为中文
            templateMap.put("category", getCategoryDisplayName(template.getTemplateType()));

            // 解析ruleCount从template_content JSON中
            try {
                if (template.getTemplateContent() != null && !template.getTemplateContent().isEmpty()) {
                    // 使用JSON解析计算实际规则数量
                    int ruleCount = calculateRuleCount(template.getTemplateContent(), template.getTemplateType());
                    templateMap.put("ruleCount", ruleCount);
                } else {
                    templateMap.put("ruleCount", 0);
                }
            } catch (Exception e) {
                log.error("解析模板内容失败，模板ID: {}, 内容: {}", template.getId(), template.getTemplateContent(), e);
                templateMap.put("ruleCount", 0);
            }

            templates.add(templateMap);
        }

        return Result.success(templates);
    }

    /**
     * 将模板类型转换为中文显示名称
     */
    private String getCategoryDisplayName(String templateType) {
        if (templateType == null || templateType.isEmpty()) {
            return "未知分类";
        }

        switch (templateType) {
            case "FIREWALL":
                return "防火墙策略";
            case "ACL":
                return "访问控制";
            case "ANTI_DDOS":
                return "DDoS防护";
            case "WAF":
                return "Web应用防护";
            case "IPS":
                return "入侵防护";
            case "NETWORK":
                return "网络安全";
            case "DATA":
                return "数据安全";
            case "SYSTEM":
                return "系统安全";
            default:
                return templateType;
        }
    }

    /**
     * 根据模板类型和内容计算规则数量
     */
    private int calculateRuleCount(String templateContent, String templateType) {
        if (templateContent == null || templateContent.isEmpty()) {
            return 0;
        }

        try {
            // 使用ObjectMapper解析JSON
            Map<String, Object> contentMap = objectMapper.readValue(templateContent, Map.class);

            if ("FIREWALL".equals(templateType)) {
                // 防火墙类型：解析rules数组
                Object rules = contentMap.get("rules");
                if (rules instanceof List) {
                    return ((List<?>) rules).size();
                }
            } else if ("ACL".equals(templateType)) {
                // 访问控制列表类型：根据实际配置计算
                int count = 0;
                if (contentMap.containsKey("ips")) {
                    Object ips = contentMap.get("ips");
                    if (ips instanceof List) {
                        count += ((List<?>) ips).size();
                    } else {
                        count++;
                    }
                }
                if (contentMap.containsKey("services")) {
                    Object services = contentMap.get("services");
                    if (services instanceof List) {
                        count += ((List<?>) services).size();
                    } else {
                        count++;
                    }
                }
                if (contentMap.containsKey("type")) {
                    count++;
                }
                return Math.max(count, 1);
            } else if ("ANTI_DDOS".equals(templateType)) {
                // DDoS防护类型：计算配置项数量
                int count = 0;
                if (contentMap.containsKey("threshold")) {
                    count++;
                }
                if (contentMap.containsKey("action")) {
                    count++;
                }
                if (contentMap.containsKey("duration")) {
                    count++;
                }
                return Math.max(count, 1);
            } else {
                // 其他类型：计算所有键的数量
                return contentMap.size();
            }
        } catch (Exception e) {
            log.warn("解析JSON失败，使用字符串匹配方法: {}", e.getMessage());
            // 如果JSON解析失败，回退到字符串匹配方法
            return calculateRuleCountByStringMatching(templateContent, templateType);
        }

        return 1; // 默认至少返回1
    }

    /**
     * 使用字符串匹配计算规则数量（备用方法）
     */
    private int calculateRuleCountByStringMatching(String templateContent, String templateType) {
        try {
            if ("FIREWALL".equals(templateType)) {
                // 计算花括号的数量来估算规则数量
                int count = 0;
                for (int i = 0; i < templateContent.length(); i++) {
                    if (templateContent.charAt(i) == '{') {
                        count++;
                    }
                }
                return Math.max(count - 1, 1); // 减去最外层的花括号
            } else {
                // 其他类型：根据逗号分隔的数量估算
                return Math.max(templateContent.split(",").length / 2, 1);
            }
        } catch (Exception e) {
            log.error("字符串匹配计算规则数量失败", e);
        }

        return 1;
    }

    /**
     * 应用策略模板
     */
    @PostMapping("/templates/{templateId}/apply")
    public Result<Void> applyPolicyTemplate(@PathVariable Long templateId, @RequestBody Map<String, Object> data) {
        log.info("应用策略模板，模板ID: {}, 数据: {}", templateId, data);
        return Result.success(null);
    }
}
