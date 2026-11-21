package com.example.securitydefense.situation.detector;

import com.example.securitydefense.situation.collector.NetFlowData;
import com.example.securitydefense.situation.collector.SecurityLogData;
import com.example.securitydefense.situation.collector.SystemLogData;
import com.example.securitydefense.situation.config.DetectionProperties;
import com.example.securitydefense.situation.model.ThreatResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 攻击链分析器
 * 关联分析多阶段攻击行为
 */
@Slf4j
@Component
public class AttackChainAnalyzer implements ThreatDetector {
    
    @Autowired
    private DetectionProperties detectionProperties;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    // 存储正在分析的攻击链（以源IP为键）
    private final Map<String, List<AttackStageEvent>> activeAttackChains = new ConcurrentHashMap<>();
    
    // MITRE ATT&CK阶段映射
    private final Map<String, String> mitreAttackStageMap = new HashMap<>();
    
    @Override
    public void initialize() {
        if (!initialized.getAndSet(true)) {
            try {
                // 初始化攻击链分析器
                log.info("Initializing Attack Chain Analyzer");
                
                // 初始化MITRE ATT&CK阶段映射
                initMitreAttackStages();
                
                // 初始化攻击链存储结构
                
                running.set(true);
                log.info("Attack Chain Analyzer initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Attack Chain Analyzer: {}", e.getMessage());
                initialized.set(false);
            }
        }
    }
    
    private void initMitreAttackStages() {
        // 初始化MITRE ATT&CK阶段映射
        mitreAttackStageMap.put("RECONNAISSANCE", "初始侦察");
        mitreAttackStageMap.put("INITIAL_ACCESS", "初始访问");
        mitreAttackStageMap.put("EXECUTION", "执行");
        mitreAttackStageMap.put("PERSISTENCE", "持久化");
        mitreAttackStageMap.put("PRIVILEGE_ESCALATION", "权限提升");
        mitreAttackStageMap.put("DEFENSE_EVASION", "防御规避");
        mitreAttackStageMap.put("CREDENTIAL_ACCESS", "凭证访问");
        mitreAttackStageMap.put("DISCOVERY", "发现");
        mitreAttackStageMap.put("LATERAL_MOVEMENT", "横向移动");
        mitreAttackStageMap.put("COLLECTION", "收集");
        mitreAttackStageMap.put("COMMAND_AND_CONTROL", "命令与控制");
        mitreAttackStageMap.put("EXFILTRATION", "数据渗漏");
        mitreAttackStageMap.put("IMPACT", "影响");
    }
    
    @Override
    public List<ThreatResult> detect(Object data) {
        List<Object> dataList = Collections.singletonList(data);
        return detectBatch(dataList);
    }
    
    @Override
    public List<ThreatResult> detectBatch(List<Object> dataList) {
        List<ThreatResult> threatResults = new ArrayList<>();
        
        if (!isAvailable()) {
            log.warn("Attack Chain Analyzer is not available, skipping detection");
            return threatResults;
        }
        
        try {
            // 处理所有输入数据
            processInputData(dataList);
            
            // 分析攻击链
            threatResults.addAll(analyzeAttackChains());
            
            // 清理过期的攻击链数据
            cleanupExpiredAttackChains();
            
        } catch (Exception e) {
            log.error("Error during attack chain analysis: {}", e.getMessage());
        }
        
        return threatResults;
    }
    
    private void processInputData(List<Object> dataList) {
        for (Object data : dataList) {
            if (data instanceof NetFlowData) {
                processNetFlowData((NetFlowData) data);
            } else if (data instanceof SecurityLogData) {
                processSecurityLogData((SecurityLogData) data);
            } else if (data instanceof SystemLogData) {
                processSystemLogData((SystemLogData) data);
            }
        }
    }
    
    private void processNetFlowData(NetFlowData netFlowData) {
        String sourceIp = netFlowData.getSourceIp();
        String destIp = netFlowData.getDestIp();
        Integer destPort = netFlowData.getDestPort();
        
        // 检测可能的侦察行为（端口扫描）
        if (isPotentialPortScan(netFlowData)) {
            addAttackStageEvent(sourceIp, destIp, "RECONNAISSANCE", "可能的端口扫描行为", netFlowData.getTimestamp());
        }
        
        // 检测可能的C2通信
        if (isPotentialC2Communication(netFlowData)) {
            addAttackStageEvent(sourceIp, destIp, "COMMAND_AND_CONTROL", "可能的命令与控制通信", netFlowData.getTimestamp());
        }
    }
    
    private void processSecurityLogData(SecurityLogData securityLogData) {
        String sourceIp = securityLogData.getSourceIp();
        String destIp = securityLogData.getDestIp();
        
        // 根据安全事件类型确定攻击阶段
        String attackStage = determineAttackStageFromSecurityLog(securityLogData);
        if (attackStage != null) {
            addAttackStageEvent(sourceIp, destIp, attackStage, securityLogData.getMessage(), securityLogData.getTimestamp());
        }
    }
    
    private void processSystemLogData(SystemLogData systemLogData) {
        String hostname = systemLogData.getHostname();
        String eventType = systemLogData.getEventType();
        
        // 根据系统事件类型确定攻击阶段
        String attackStage = determineAttackStageFromSystemLog(systemLogData);
        if (attackStage != null) {
            // 使用主机名作为目标标识
            addAttackStageEvent(systemLogData.getSourceIp(), hostname, attackStage, systemLogData.getMessage(), systemLogData.getTimestamp());
        }
    }
    
    private void addAttackStageEvent(String source, String target, String stage, String description, long timestamp) {
        String key = source + "_" + target;
        activeAttackChains.computeIfAbsent(key, k -> new ArrayList<>())
                .add(new AttackStageEvent(source, target, stage, description, timestamp));
        
        log.debug("Added attack stage event: source={}, target={}, stage={}", source, target, stage);
    }
    
    private void addAttackStageEvent(String source, String target, String stage, String description, java.time.LocalDateTime timestamp) {
        // 将LocalDateTime转换为毫秒时间戳
        long timestampMillis = timestamp.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        addAttackStageEvent(source, target, stage, description, timestampMillis);
    }
    
    private List<ThreatResult> analyzeAttackChains() {
        List<ThreatResult> results = new ArrayList<>();
        
        for (Map.Entry<String, List<AttackStageEvent>> entry : activeAttackChains.entrySet()) {
            String key = entry.getKey();
            List<AttackStageEvent> events = entry.getValue();
            
            // 分析攻击链的完整性和严重性
            AttackChainAnalysisResult analysisResult = analyzeAttackChainEvents(events);
            
            if (analysisResult.isAttackChainDetected()) {
                ThreatResult result = createThreatResultFromAnalysis(key, events, analysisResult);
                results.add(result);
                
                log.warn("Detected potential attack chain: {}", result.getDescription());
            }
        }
        
        return results;
    }
    
    private AttackChainAnalysisResult analyzeAttackChainEvents(List<AttackStageEvent> events) {
        // 按阶段分组
        Map<String, List<AttackStageEvent>> eventsByStage = events.stream()
                .collect(Collectors.groupingBy(AttackStageEvent::getStage));
        
        // 检查是否检测到完整的攻击链
        boolean reconnaissanceDetected = eventsByStage.containsKey("RECONNAISSANCE");
        boolean initialAccessDetected = eventsByStage.containsKey("INITIAL_ACCESS");
        boolean executionDetected = eventsByStage.containsKey("EXECUTION");
        boolean privilegeEscalationDetected = eventsByStage.containsKey("PRIVILEGE_ESCALATION");
        boolean lateralMovementDetected = eventsByStage.containsKey("LATERAL_MOVEMENT");
        boolean exfiltrationDetected = eventsByStage.containsKey("EXFILTRATION");
        
        // 计算攻击链分数（基于检测到的阶段数量和严重性）
        int detectedStages = 0;
        if (reconnaissanceDetected) detectedStages++;
        if (initialAccessDetected) detectedStages++;
        if (executionDetected) detectedStages++;
        if (privilegeEscalationDetected) detectedStages++;
        if (lateralMovementDetected) detectedStages++;
        if (exfiltrationDetected) detectedStages++;
        
        // 计算严重性分数（越后的阶段权重越高）
        double severityScore = 0;
        if (reconnaissanceDetected) severityScore += 10;
        if (initialAccessDetected) severityScore += 20;
        if (executionDetected) severityScore += 25;
        if (privilegeEscalationDetected) severityScore += 30;
        if (lateralMovementDetected) severityScore += 40;
        if (exfiltrationDetected) severityScore += 50;
        
        // 确定是否检测到攻击链（至少2个不同阶段）
        boolean attackChainDetected = detectedStages >= 2;
        
        // 确定攻击链状态
        String attackChainStatus = determineAttackChainStatus(eventsByStage);
        
        return new AttackChainAnalysisResult(attackChainDetected, detectedStages, severityScore, attackChainStatus);
    }
    
    private String determineAttackChainStatus(Map<String, List<AttackStageEvent>> eventsByStage) {
        // 确定攻击链的当前状态
        if (eventsByStage.containsKey("EXFILTRATION")) {
            return "DATA_BREACHED";
        } else if (eventsByStage.containsKey("LATERAL_MOVEMENT")) {
            return "MOVING_LATERALLY";
        } else if (eventsByStage.containsKey("PRIVILEGE_ESCALATION")) {
            return "PRIVILEGE_ESCALATED";
        } else if (eventsByStage.containsKey("EXECUTION")) {
            return "EXECUTING_CODE";
        } else if (eventsByStage.containsKey("INITIAL_ACCESS")) {
            return "INITIAL_ACCESS_GAINED";
        } else {
            return "RECONNAISSANCE";
        }
    }
    
    private ThreatResult createThreatResultFromAnalysis(String key, List<AttackStageEvent> events, 
                                                      AttackChainAnalysisResult analysisResult) {
        String[] parts = key.split("_");
        String source = parts[0];
        String target = parts[1];
        
        // 获取所有检测到的阶段
        Set<String> detectedStages = events.stream()
                .map(AttackStageEvent::getStage)
                .collect(Collectors.toSet());
        
        // 构建描述
        StringBuilder description = new StringBuilder();
        description.append("检测到攻击链：源 ")
                .append(source)
                .append(" 针对目标 ")
                .append(target)
                .append(". 检测到 ")
                .append(analysisResult.getDetectedStages())
                .append(" 个攻击阶段：");
        
        for (String stage : detectedStages) {
            description.append(" ")
                    .append(mitreAttackStageMap.getOrDefault(stage, stage));
        }
        
        ThreatResult result = new ThreatResult();
        result.setDetectionId("ATTACK_CHAIN-" + UUID.randomUUID().toString().substring(0, 8));
        result.setTimestamp(System.currentTimeMillis());
        result.setThreatType("MULTI_STAGE_ATTACK");
        result.setThreatLevel(analysisResult.getSeverityScore() > 80 ? "HIGH" : 
                             analysisResult.getSeverityScore() > 40 ? "MEDIUM" : "LOW");
        result.setSourceIp(source);
        result.setDestIp(target);
        result.setDescription(description.toString());
        result.setDetectionScore(analysisResult.getSeverityScore());
        result.setDetectorName(getDetectorName());
        result.setRecommendedAction(getRecommendedAction(analysisResult.getAttackChainStatus()));
        
        return result;
    }
    
    private String getRecommendedAction(String attackChainStatus) {
        switch (attackChainStatus) {
            case "DATA_BREACHED":
                return "立即隔离受影响系统，启动事件响应流程";
            case "MOVING_LATERALLY":
                return "隔离受感染主机，检查横向移动路径";
            case "PRIVILEGE_ESCALATED":
                return "重置受影响账户密码，检查权限变更";
            case "EXECUTING_CODE":
                return "终止可疑进程，隔离受感染系统";
            case "INITIAL_ACCESS_GAINED":
                return "阻断初始访问源，检查入侵痕迹";
            default:
                return "监控可疑活动，准备响应措施";
        }
    }
    
    private boolean isPotentialPortScan(NetFlowData netFlowData) {
        // 简化的端口扫描检测逻辑
        // 实际项目中应使用更复杂的算法
        return netFlowData.getPacketCount() < 10 && netFlowData.getByteCount() < 1024;
    }
    
    private boolean isPotentialC2Communication(NetFlowData netFlowData) {
        // 简化的C2通信检测逻辑
        // 实际项目中应使用更复杂的算法和已知C2特征
        int destPort = netFlowData.getDestPort();
        return (destPort > 10000 && destPort < 65535) && 
               netFlowData.getPacketCount() > 0 && netFlowData.getByteCount() > 0;
    }
    
    private String determineAttackStageFromSecurityLog(SecurityLogData securityLogData) {
        String eventType = securityLogData.getEventType();
        String message = securityLogData.getMessage().toLowerCase();
        
        if ("BLOCK".equals(eventType) && message.contains("scan")) {
            return "RECONNAISSANCE";
        } else if ("BLOCK".equals(eventType) && (message.contains("exploit") || message.contains("shell"))) {
            return "INITIAL_ACCESS";
        } else if ("ALERT".equals(eventType) && message.contains("execution")) {
            return "EXECUTION";
        } else if ("ALERT".equals(eventType) && message.contains("privilege")) {
            return "PRIVILEGE_ESCALATION";
        } else if ("ALERT".equals(eventType) && message.contains("lateral")) {
            return "LATERAL_MOVEMENT";
        }
        
        return null;
    }
    
    private String determineAttackStageFromSystemLog(SystemLogData systemLogData) {
        String eventType = systemLogData.getEventType();
        
        switch (eventType) {
            case "LOGIN_FAILURE":
                return "RECONNAISSANCE";
            case "LOGIN_SUCCESS":
                return "INITIAL_ACCESS";
            case "PROCESS_CREATE":
                return "EXECUTION";
            case "PRIVILEGE_ESCALATION":
                return "PRIVILEGE_ESCALATION";
            case "CONFIG_CHANGE":
                return "PERSISTENCE";
            default:
                return null;
        }
    }
    
    private void cleanupExpiredAttackChains() {
        // 清理过期的攻击链数据
        long currentTime = System.currentTimeMillis();
        long analysisWindowMs = detectionProperties.getAttackChainWindow() * 60 * 1000; // 转换为毫秒
        
        activeAttackChains.entrySet().removeIf(entry -> {
            List<AttackStageEvent> events = entry.getValue();
            // 检查是否有事件在分析窗口内
            return events.stream().noneMatch(event -> currentTime - event.getTimestamp() < analysisWindowMs);
        });
    }
    
    @Override
    public void updateDetectionRules(Map<String, Object> updateData) {
        // 更新检测规则
        log.info("Updating attack chain analysis rules: {}", updateData);
        // 这里添加规则更新代码
    }
    
    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("initialized", initialized.get());
        status.put("running", running.get());
        status.put("activeAttackChains", activeAttackChains.size());
        status.put("analysisWindowMinutes", detectionProperties.getAttackChainWindow());
        return status;
    }
    
    @Override
    public String getDetectorName() {
        return "Attack Chain Analyzer";
    }
    
    @Override
    public void shutdown() {
        if (initialized.getAndSet(false)) {
            running.set(false);
            activeAttackChains.clear();
            log.info("Attack Chain Analyzer shutdown");
        }
    }
    
    @Override
    public boolean isAvailable() {
        return initialized.get() && running.get();
    }
    
    /**
     * 攻击阶段事件内部类
     */
    private static class AttackStageEvent {
        private final String source;
        private final String target;
        private final String stage;
        private final String description;
        private final long timestamp;
        
        public AttackStageEvent(String source, String target, String stage, String description, long timestamp) {
            this.source = source;
            this.target = target;
            this.stage = stage;
            this.description = description;
            this.timestamp = timestamp;
        }
        
        public String getSource() { return source; }
        public String getTarget() { return target; }
        public String getStage() { return stage; }
        public String getDescription() { return description; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * 攻击链分析结果内部类
     */
    private static class AttackChainAnalysisResult {
        private final boolean attackChainDetected;
        private final int detectedStages;
        private final double severityScore;
        private final String attackChainStatus;
        
        public AttackChainAnalysisResult(boolean attackChainDetected, int detectedStages, 
                                       double severityScore, String attackChainStatus) {
            this.attackChainDetected = attackChainDetected;
            this.detectedStages = detectedStages;
            this.severityScore = severityScore;
            this.attackChainStatus = attackChainStatus;
        }
        
        public boolean isAttackChainDetected() { return attackChainDetected; }
        public int getDetectedStages() { return detectedStages; }
        public double getSeverityScore() { return severityScore; }
        public String getAttackChainStatus() { return attackChainStatus; }
    }
}