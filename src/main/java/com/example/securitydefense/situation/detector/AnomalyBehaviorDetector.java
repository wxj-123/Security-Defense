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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 异常行为检测器
 * 基于机器学习算法识别异常访问模式
 */
@Slf4j
@Component
public class AnomalyBehaviorDetector implements ThreatDetector {
    
    @Autowired
    private DetectionProperties detectionProperties;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private long lastModelUpdateTime;
    
    // 存储历史数据用于基线计算
    private final Map<String, List<Double>> baselineData = new HashMap<>();
    
    @Override
    public void initialize() {
        if (!initialized.getAndSet(true)) {
            try {
                // 初始化异常行为检测器
                log.info("Initializing Anomaly Behavior Detector");
                
                // 初始化基线数据存储结构
                initBaselineData();
                
                // 这里可以添加机器学习模型初始化代码
                // 例如：加载预训练模型、初始化孤立森林算法等
                
                running.set(true);
                lastModelUpdateTime = System.currentTimeMillis();
                log.info("Anomaly Behavior Detector initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Anomaly Behavior Detector: {}", e.getMessage());
                initialized.set(false);
            }
        }
    }
    
    private void initBaselineData() {
        // 初始化各种指标的基线数据存储
        baselineData.put("network_traffic_bytes", new ArrayList<>());
        baselineData.put("network_traffic_packets", new ArrayList<>());
        baselineData.put("login_attempts", new ArrayList<>());
        baselineData.put("process_creations", new ArrayList<>());
        baselineData.put("failed_logins", new ArrayList<>());
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
            log.warn("Anomaly Behavior Detector is not available, skipping detection");
            return threatResults;
        }
        
        try {
            // 检查是否需要更新模型
            checkAndUpdateModel();
            
            // 分类处理不同类型的数据
            List<NetFlowData> netFlowDataList = filterDataByType(dataList, NetFlowData.class);
            List<SecurityLogData> securityLogDataList = filterDataByType(dataList, SecurityLogData.class);
            List<SystemLogData> systemLogDataList = filterDataByType(dataList, SystemLogData.class);
            
            // 检测网络流量异常
            threatResults.addAll(detectNetworkAnomalies(netFlowDataList));
            
            // 检测安全日志异常
            threatResults.addAll(detectSecurityLogAnomalies(securityLogDataList));
            
            // 检测系统日志异常
            threatResults.addAll(detectSystemLogAnomalies(systemLogDataList));
            
        } catch (Exception e) {
            log.error("Error during anomaly detection: {}", e.getMessage());
        }
        
        return threatResults;
    }
    
    private <T> List<T> filterDataByType(List<Object> dataList, Class<T> type) {
        return dataList.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }
    
    private List<ThreatResult> detectNetworkAnomalies(List<NetFlowData> netFlowDataList) {
        List<ThreatResult> results = new ArrayList<>();
        
        // 按源IP分组统计流量
        Map<String, List<NetFlowData>> flowsBySourceIp = netFlowDataList.stream()
                .collect(Collectors.groupingBy(NetFlowData::getSourceIp));
        
        for (Map.Entry<String, List<NetFlowData>> entry : flowsBySourceIp.entrySet()) {
            String sourceIp = entry.getKey();
            List<NetFlowData> flows = entry.getValue();
            
            // 计算总流量和数据包数量
            long totalBytes = flows.stream().mapToLong(NetFlowData::getByteCount).sum();
            long totalPackets = flows.stream().mapToLong(NetFlowData::getPacketCount).sum();
            
            // 检测异常流量（这里使用简化的阈值检测，实际应使用机器学习算法）
            double anomalyScore = calculateNetworkAnomalyScore(totalBytes, totalPackets);
            
            if (anomalyScore > detectionProperties.getAnomalyThreshold()) {
                ThreatResult result = new ThreatResult();
                result.setDetectionId("ANOMALY-" + UUID.randomUUID().toString().substring(0, 8));
                result.setTimestamp(System.currentTimeMillis());
                result.setThreatType("NETWORK_ANOMALY");
                result.setThreatLevel(getThreatLevelByScore(anomalyScore));
                result.setSourceIp(sourceIp);
                result.setDescription("异常网络流量检测：源IP " + sourceIp + " 产生了异常流量");
                result.setDetectionScore(anomalyScore);
                result.setDetectorName(getDetectorName());
                result.setRecommendedAction("检查该IP的流量模式，考虑临时阻断");
                
                results.add(result);
            }
        }
        
        return results;
    }
    
    private List<ThreatResult> detectSecurityLogAnomalies(List<SecurityLogData> securityLogDataList) {
        List<ThreatResult> results = new ArrayList<>();
        
        // 统计高风险安全事件
        List<SecurityLogData> highRiskEvents = securityLogDataList.stream()
                .filter(log -> "CRITICAL".equals(log.getLogLevel()) || "ERROR".equals(log.getLogLevel()))
                .collect(Collectors.toList());
        
        // 按源IP分组统计高风险事件
        Map<String, Long> highRiskEventsByIp = highRiskEvents.stream()
                .collect(Collectors.groupingBy(SecurityLogData::getSourceIp, Collectors.counting()));
        
        for (Map.Entry<String, Long> entry : highRiskEventsByIp.entrySet()) {
            String sourceIp = entry.getKey();
            long eventCount = entry.getValue();
            
            // 检测异常的高风险事件数量
            if (eventCount > 5) { // 简化的阈值检测
                ThreatResult result = new ThreatResult();
                result.setDetectionId("ANOMALY-" + UUID.randomUUID().toString().substring(0, 8));
                result.setTimestamp(System.currentTimeMillis());
                result.setThreatType("SECURITY_LOG_ANOMALY");
                result.setThreatLevel("HIGH");
                result.setSourceIp(sourceIp);
                result.setDescription("安全日志异常检测：源IP " + sourceIp + " 产生了 " + eventCount + " 个高风险安全事件");
                result.setDetectionScore(85.0 + eventCount * 2); // 简化的分数计算
                result.setDetectorName(getDetectorName());
                result.setRecommendedAction("立即调查该IP的活动，可能存在攻击行为");
                
                results.add(result);
            }
        }
        
        return results;
    }
    
    private List<ThreatResult> detectSystemLogAnomalies(List<SystemLogData> systemLogDataList) {
        List<ThreatResult> results = new ArrayList<>();
        
        // 统计登录失败事件
        List<SystemLogData> failedLogins = systemLogDataList.stream()
                .filter(log -> "LOGIN_FAILURE".equals(log.getEventType()))
                .collect(Collectors.toList());
        
        // 按用户名和源IP分组统计登录失败
        Map<String, Long> failedLoginsByUserIp = failedLogins.stream()
                .collect(Collectors.groupingBy(log -> log.getUsername() + "_" + log.getSourceIp(), Collectors.counting()));
        
        for (Map.Entry<String, Long> entry : failedLoginsByUserIp.entrySet()) {
            String[] parts = entry.getKey().split("_");
            String username = parts[0];
            String sourceIp = parts[1];
            long failureCount = entry.getValue();
            
            // 检测异常的登录失败尝试
            if (failureCount > 3) { // 简化的阈值检测
                ThreatResult result = new ThreatResult();
                result.setDetectionId("ANOMALY-" + UUID.randomUUID().toString().substring(0, 8));
                result.setTimestamp(System.currentTimeMillis());
                result.setThreatType("BRUTE_FORCE_ATTEMPT");
                result.setThreatLevel(failureCount > 10 ? "HIGH" : "MEDIUM");
                result.setSourceIp(sourceIp);
                result.setDescription("暴力破解尝试检测：用户 " + username + " 从IP " + sourceIp + " 有 " + failureCount + " 次登录失败");
                result.setDetectionScore(70.0 + failureCount * 3); // 简化的分数计算
                result.setDetectorName(getDetectorName());
                result.setRecommendedAction("暂时锁定该账户，调查登录尝试");
                
                results.add(result);
            }
        }
        
        return results;
    }
    
    private double calculateNetworkAnomalyScore(long bytes, long packets) {
        // 简化的网络异常评分算法
        // 实际项目中应使用孤立森林等机器学习算法
        double score = 0.0;
        
        // 基于流量大小的评分
        if (bytes > 100_000_000) { // 100MB
            score += 40.0;
        } else if (bytes > 50_000_000) { // 50MB
            score += 25.0;
        } else if (bytes > 10_000_000) { // 10MB
            score += 10.0;
        }
        
        // 基于数据包数量的评分
        if (packets > 100_000) {
            score += 40.0;
        } else if (packets > 50_000) {
            score += 25.0;
        } else if (packets > 10_000) {
            score += 10.0;
        }
        
        // 基于流量密度的评分（字节/包）
        double bytesPerPacket = packets > 0 ? (double) bytes / packets : 0;
        if (bytesPerPacket < 64) { // 可能的扫描行为
            score += 20.0;
        } else if (bytesPerPacket > 1500) { // 可能的大数据传输
            score += 10.0;
        }
        
        return Math.min(100.0, score);
    }
    
    private String getThreatLevelByScore(double score) {
        if (score > 80) {
            return "HIGH";
        } else if (score > 60) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    private void checkAndUpdateModel() {
        // 检查是否需要更新机器学习模型
        long currentTime = System.currentTimeMillis();
        long modelUpdateInterval = detectionProperties.getModelUpdateInterval() * 60 * 60 * 1000; // 转换为毫秒
        
        if (detectionProperties.isEnableMachineLearning() && 
                currentTime - lastModelUpdateTime > modelUpdateInterval) {
            updateModel();
            lastModelUpdateTime = currentTime;
        }
    }
    
    private void updateModel() {
        // 更新机器学习模型的逻辑
        // 实际项目中应重新训练模型或更新模型参数
        log.info("Updating anomaly detection model");
        // 这里添加模型更新代码
    }
    
    @Override
    public void updateDetectionRules(Map<String, Object> updateData) {
        // 更新检测规则
        log.info("Updating detection rules: {}", updateData);
        // 这里添加规则更新代码
    }
    
    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("initialized", initialized.get());
        status.put("running", running.get());
        status.put("lastModelUpdateTime", lastModelUpdateTime);
        status.put("mlEnabled", detectionProperties.isEnableMachineLearning());
        status.put("anomalyThreshold", detectionProperties.getAnomalyThreshold());
        return status;
    }
    
    @Override
    public String getDetectorName() {
        return "Anomaly Behavior Detector";
    }
    
    @Override
    public void shutdown() {
        if (initialized.getAndSet(false)) {
            running.set(false);
            log.info("Anomaly Behavior Detector shutdown");
            // 这里添加资源释放逻辑
        }
    }
    
    @Override
    public boolean isAvailable() {
        return initialized.get() && running.get();
    }
}