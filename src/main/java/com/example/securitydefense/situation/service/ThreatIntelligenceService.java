package com.example.securitydefense.situation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.securitydefense.situation.model.entity.ThreatIntelligenceEntity;
import com.example.securitydefense.situation.repository.ThreatIntelligenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 威胁情报服务类
 * 负责威胁情报的数据库查询和业务逻辑处理
 */
@Service
public class ThreatIntelligenceService {
    
    @Autowired
    private ThreatIntelligenceRepository threatIntelligenceRepository;
    
    /**
     * 根据ID获取威胁情报详情
     * @param id 威胁情报ID
     * @return 威胁情报详情
     */
    public Map<String, Object> getThreatDetailById(Long id) {
        ThreatIntelligenceEntity entity = threatIntelligenceRepository.selectById(id);
        if (entity == null) {
            return null;
        }
        
        return convertEntityToMap(entity);
    }
    
    /**
     * 根据威胁ID字符串获取威胁情报详情
     * @param threatId 威胁ID字符串（可能包含THREAT-前缀）
     * @return 威胁情报详情
     */
    public Map<String, Object> getThreatDetailByThreatId(String threatId) {
        // 尝试解析数字ID
        Long id = null;
        try {
            // 如果threatId是"THREAT-0001"格式，提取数字部分
            if (threatId != null && threatId.startsWith("THREAT-")) {
                String numericPart = threatId.substring("THREAT-".length());
                id = Long.parseLong(numericPart);
            } else {
                // 直接尝试解析为数字
                id = Long.parseLong(threatId);
            }
        } catch (NumberFormatException e) {
            // 如果解析失败，尝试通过ioc_value查找
            QueryWrapper<ThreatIntelligenceEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("ioc_value", threatId);
            wrapper.last("LIMIT 1");
            ThreatIntelligenceEntity entity = threatIntelligenceRepository.selectOne(wrapper);
            if (entity != null) {
                return convertEntityToMap(entity);
            }
            return null;
        }
        
        return getThreatDetailById(id);
    }
    
    /**
     * 将实体对象转换为Map
     * @param entity 威胁情报实体
     * @return 转换后的Map
     */
    private Map<String, Object> convertEntityToMap(ThreatIntelligenceEntity entity) {
        Map<String, Object> detail = new HashMap<>();

        // 基本信息
        detail.put("id", entity.getId());
        detail.put("threatId", "THREAT-" + String.format("%04d", entity.getId()));
        detail.put("iocValue", entity.getIocValue());
        detail.put("iocType", convertIocType(entity.getIocType()));
        detail.put("threatType", entity.getThreatType());
        detail.put("severity", convertThreatLevel(entity.getThreatLevel()));
        detail.put("confidenceScore", entity.getConfidenceScore() != null ? entity.getConfidenceScore().intValue() : 0);
        detail.put("source", entity.getSource());
        detail.put("description", entity.getDescription());
        detail.put("provider", "安全运营中心"); // 添加provider字段

        // 时间信息
        detail.put("firstSeen", entity.getFirstSeen() != null ? entity.getFirstSeen().toString() : "");
        detail.put("lastUpdated", entity.getUpdateTime() != null ? entity.getUpdateTime().toString() : "");
        detail.put("discoveredTime", entity.getCreateTime() != null ? entity.getCreateTime().toString() : "");

        // 标签和状态
        detail.put("status", entity.getStatus() != null && "ACTIVE".equals(entity.getStatus()) ? "ACTIVE" : "INACTIVE");

        // 生成标签
        List<String> tags = new ArrayList<>();
        if (entity.getThreatType() != null) {
            tags.add(entity.getThreatType());
        }
        if (entity.getIocType() != null) {
            tags.add(convertIocType(entity.getIocType()));
        }
        if (entity.getLocationInfo() != null && !entity.getLocationInfo().isEmpty()) {
            tags.add(entity.getLocationInfo());
        }
        detail.put("tags", tags);

        // 相关CVE
        List<String> relatedCves = new ArrayList<>();
        if (entity.getRelatedCve() != null && !entity.getRelatedCve().isEmpty()) {
            String[] cveArray = entity.getRelatedCve().split(",");
            for (String cve : cveArray) {
                cve = cve.trim();
                if (!cve.isEmpty()) {
                    relatedCves.add(cve);
                }
            }
        }
        detail.put("relatedCves", relatedCves);

        // 影响系统
        List<String> affectedSystems = new ArrayList<>();
        if (entity.getLocationInfo() != null && !entity.getLocationInfo().isEmpty()) {
            affectedSystems.add(entity.getLocationInfo());
        }
        detail.put("affectedSystems", affectedSystems);

        // 附加信息
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("country", entity.getLocationInfo() != null ? entity.getLocationInfo() : "Unknown");
        additionalInfo.put("asn", "AS" + (entity.getId() % 65000 + 1000));
        additionalInfo.put("attackPattern", "T" + (entity.getId() % 2000));
        additionalInfo.put("toolsUsed", entity.getThreatType());
        additionalInfo.put("historicalActivities", (int) (Math.random() * 50) + 10);
        detail.put("additionalInfo", additionalInfo);

        // 生成处置建议
        List<Map<String, String>> recommendations = generateRecommendations(entity);
        detail.put("recommendations", recommendations);

        // 受影响资产（模拟数据）
        List<Map<String, Object>> affectedAssets = generateAffectedAssets(entity);
        detail.put("affectedAssets", affectedAssets);

        // 影响事件数（模拟数据）
        detail.put("affectedEvents", generateAffectedEvents(entity));

        // 活动时间线（模拟数据）
        List<Map<String, Object>> activities = generateActivities(entity);
        detail.put("activities", activities);

        // 相关威胁（模拟数据）
        List<Map<String, Object>> relatedThreats = generateRelatedThreats(entity);
        detail.put("relatedThreats", relatedThreats);

        return detail;
    }
    
    /**
     * 转换IOC类型为标准格式
     */
    private String convertIocType(String iocType) {
        if (iocType == null) return "UNKNOWN";
        switch (iocType.toUpperCase()) {
            case "IP":
                return "IP";
            case "DOMAIN":
                return "DOMAIN";
            case "URL":
                return "URL";
            case "HASH":
            case "FILE_HASH":
                return "FILE_HASH";
            case "EMAIL":
                return "EMAIL";
            default:
                return iocType.toUpperCase();
        }
    }
    
    /**
     * 转换威胁级别为标准格式
     */
    private String convertThreatLevel(String threatLevel) {
        if (threatLevel == null) return "MEDIUM";
        switch (threatLevel.toUpperCase()) {
            case "CRITICAL":
            case "4":
                return "CRITICAL";
            case "HIGH":
            case "3":
                return "HIGH";
            case "MEDIUM":
            case "2":
                return "MEDIUM";
            case "LOW":
            case "1":
                return "LOW";
            default:
                return "MEDIUM";
        }
    }
    
    /**
     * 生成处置建议
     */
    private List<Map<String, String>> generateRecommendations(ThreatIntelligenceEntity entity) {
        List<Map<String, String>> recommendations = new ArrayList<>();
        
        String iocType = convertIocType(entity.getIocType());
        
        // 根据IOC类型生成不同的建议
        if ("IP".equals(iocType)) {
            Map<String, String> rec1 = new HashMap<>();
            rec1.put("title", "立即封禁IP");
            rec1.put("detail", "在防火墙和边界设备上封禁此IP地址，阻止其访问内部网络。");
            recommendations.add(rec1);
        } else if ("DOMAIN".equals(iocType) || "URL".equals(iocType)) {
            Map<String, String> rec1 = new HashMap<>();
            rec1.put("title", "屏蔽恶意域名");
            rec1.put("detail", "在DNS服务器上添加恶意域名黑名单，防止用户访问。");
            recommendations.add(rec1);
        } else if ("FILE_HASH".equals(iocType)) {
            Map<String, String> rec1 = new HashMap<>();
            rec1.put("title", "隔离恶意文件");
            rec1.put("detail", "在终端安全系统中添加该文件的哈希黑名单，进行自动隔离。");
            recommendations.add(rec1);
        }
        
        // 通用建议
        Map<String, String> rec2 = new HashMap<>();
        rec2.put("title", "增强安全监控");
        rec2.put("detail", "加强对相关系统和流量的监控，及时发现异常行为。");
        recommendations.add(rec2);
        
        Map<String, String> rec3 = new HashMap<>();
        rec3.put("title", "通知安全团队");
        rec3.put("detail", "立即通知安全运营团队，进行深入调查和响应。");
        recommendations.add(rec3);
        
        Map<String, String> rec4 = new HashMap<>();
        rec4.put("title", "更新防护策略");
        rec4.put("detail", "根据此威胁的特征更新安全防护策略和规则。");
        recommendations.add(rec4);
        
        return recommendations;
    }
    
    /**
     * 生成受影响资产（模拟数据）
     */
    private List<Map<String, Object>> generateAffectedAssets(ThreatIntelligenceEntity entity) {
        List<Map<String, Object>> affectedAssets = new ArrayList<>();
        
        String[] assetTypes = {"服务器", "数据库", "网络设备", "工作站"};
        
        int assetCount = (int) (Math.random() * 3) + 1;
        for (int i = 0; i < assetCount; i++) {
            Map<String, Object> asset = new HashMap<>();
            asset.put("assetId", "ASSET-" + String.format("%03d", entity.getId() * 10 + i + 1));
            asset.put("assetName", assetTypes[i % assetTypes.length] + "-" + String.format("%02d", entity.getId() * 10 + i + 1));
            asset.put("assetType", "SERVER");
            asset.put("riskScore", 60 + (int) (Math.random() * 30));
            asset.put("riskLevel", "MEDIUM");
            asset.put("ipAddress", "10.0.0." + ((entity.getId() * 10 + i + 1) % 255));
            affectedAssets.add(asset);
        }
        
        return affectedAssets;
    }
    
    /**
     * 生成影响事件（模拟数据）
     */
    private List<Map<String, Object>> generateAffectedEvents(ThreatIntelligenceEntity entity) {
        List<Map<String, Object>> events = new ArrayList<>();
        
        Map<String, Object> event1 = new HashMap<>();
        event1.put("id", "EVENT-" + String.format("%06d", entity.getId() * 100 + 1));
        event1.put("type", "检测告警");
        event1.put("count", (int) (Math.random() * 100) + 50);
        event1.put("description", "系统检测到与该IOC相关的可疑活动");
        events.add(event1);
        
        Map<String, Object> event2 = new HashMap<>();
        event2.put("id", "EVENT-" + String.format("%06d", entity.getId() * 100 + 2));
        event2.put("type", "访问尝试");
        event2.put("count", (int) (Math.random() * 50) + 20);
        event2.put("description", "检测到来自该源的访问尝试");
        events.add(event2);
        
        return events;
    }
    
    /**
     * 生成活动时间线（模拟数据）
     */
    private List<Map<String, Object>> generateActivities(ThreatIntelligenceEntity entity) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        Date now = new Date();
        
        Map<String, Object> activity1 = new HashMap<>();
        activity1.put("timestamp", now.toString());
        activity1.put("type", "update");
        activity1.put("title", "情报更新");
        activity1.put("description", "威胁情报置信度更新为" + entity.getConfidenceScore());
        activities.add(activity1);
        
        Map<String, Object> activity2 = new HashMap<>();
        activity2.put("timestamp", new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000L).toString());
        activity2.put("type", "detection");
        activity2.put("title", "新检测");
        activity2.put("description", "在系统日志中发现新的相关活动");
        activities.add(activity2);
        
        Map<String, Object> activity3 = new HashMap<>();
        activity3.put("timestamp", entity.getFirstSeen() != null ? entity.getFirstSeen().toString() : now.toString());
        activity3.put("type", "creation");
        activity3.put("title", "情报创建");
        activity3.put("description", "首次发现该威胁指标");
        activities.add(activity3);
        
        return activities;
    }
    
    /**
     * 生成相关威胁（模拟数据）
     */
    private List<Map<String, Object>> generateRelatedThreats(ThreatIntelligenceEntity entity) {
        List<Map<String, Object>> relatedThreats = new ArrayList<>();
        
        // 查询相关威胁（相同类型或相同来源的威胁）
        QueryWrapper<ThreatIntelligenceEntity> wrapper = new QueryWrapper<>();
        wrapper.ne("id", entity.getId());
        wrapper.and(w -> w.eq("threat_type", entity.getThreatType())
                .or()
                .eq("source", entity.getSource()));
        wrapper.last("LIMIT 5");
        
        List<ThreatIntelligenceEntity> relatedEntities = threatIntelligenceRepository.selectList(wrapper);
        
        for (ThreatIntelligenceEntity relatedEntity : relatedEntities) {
            Map<String, Object> threat = new HashMap<>();
            threat.put("id", relatedEntity.getId());
            threat.put("threatId", "THREAT-" + String.format("%04d", relatedEntity.getId()));
            threat.put("iocValue", relatedEntity.getIocValue());
            threat.put("iocType", convertIocType(relatedEntity.getIocType()));
            threat.put("threatType", relatedEntity.getThreatType());
            threat.put("severity", convertThreatLevel(relatedEntity.getThreatLevel()));
            threat.put("confidenceScore", relatedEntity.getConfidenceScore() != null ? relatedEntity.getConfidenceScore().intValue() : 0);
            threat.put("lastUpdated", relatedEntity.getUpdateTime() != null ? relatedEntity.getUpdateTime().toString() : "");
            threat.put("description", relatedEntity.getDescription());
            relatedThreats.add(threat);
        }
        
        return relatedThreats;
    }
    
    /**
     * 获取威胁影响资产
     * @param threatId 威胁ID
     * @return 受影响资产列表
     */
    public List<Map<String, Object>> getThreatAffectedAssets(String threatId) {
        Map<String, Object> threatDetail = getThreatDetailByThreatId(threatId);
        if (threatDetail == null) {
            return new ArrayList<>();
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> affectedAssets = (List<Map<String, Object>>) threatDetail.get("affectedAssets");
        return affectedAssets != null ? affectedAssets : new ArrayList<>();
    }
    
    /**
     * 获取威胁相关事件
     * @param threatId 威胁ID
     * @return 相关事件列表
     */
    public List<Map<String, Object>> getThreatRelatedEvents(String threatId) {
        Map<String, Object> threatDetail = getThreatDetailByThreatId(threatId);
        if (threatDetail == null) {
            return new ArrayList<>();
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> events = (List<Map<String, Object>>) threatDetail.get("affectedEvents");
        return events != null ? events : new ArrayList<>();
    }
    
    /**
     * 获取威胁相关情报
     * @param threatId 威胁ID
     * @return 相关情报列表
     */
    public List<Map<String, Object>> getRelatedThreats(String threatId) {
        Map<String, Object> threatDetail = getThreatDetailByThreatId(threatId);
        if (threatDetail == null) {
            return new ArrayList<>();
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> relatedThreats = (List<Map<String, Object>>) threatDetail.get("relatedThreats");
        return relatedThreats != null ? relatedThreats : new ArrayList<>();
    }
}
