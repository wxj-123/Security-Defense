package com.example.securitydefense.situation.detector;

import com.example.securitydefense.situation.collector.*;
import com.example.securitydefense.situation.config.DetectionProperties;
import com.example.securitydefense.situation.model.ThreatResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * IOC匹配检测器
 * 实时匹配威胁情报指标
 */
@Slf4j
@Component
public class IocMatcher implements ThreatDetector {
    
    @Autowired
    private DetectionProperties detectionProperties;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    // IOC数据存储 - 按类型分类
    private final Map<String, Set<String>> iocStore = new ConcurrentHashMap<>();
    
    // 正则表达式模式缓存
    private final Map<String, Pattern> regexPatternCache = new ConcurrentHashMap<>();
    
    // IOC类型权重映射
    private final Map<String, Integer> iocTypeWeightMap = new HashMap<>();
    
    @Override
    public void initialize() {
        if (!initialized.getAndSet(true)) {
            try {
                log.info("Initializing IOC Matcher");
                
                // 初始化IOC存储结构
                initIocStores();
                
                // 初始化IOC类型权重
                initIocTypeWeights();
                
                running.set(true);
                log.info("IOC Matcher initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize IOC Matcher: {}", e.getMessage());
                initialized.set(false);
            }
        }
    }
    
    private void initIocStores() {
        // 初始化各种IOC类型的存储
        iocStore.put("IP", ConcurrentHashMap.newKeySet());
        iocStore.put("DOMAIN", ConcurrentHashMap.newKeySet());
        iocStore.put("URL", ConcurrentHashMap.newKeySet());
        iocStore.put("FILE_HASH", ConcurrentHashMap.newKeySet());
        iocStore.put("EMAIL", ConcurrentHashMap.newKeySet());
        iocStore.put("REGEX", ConcurrentHashMap.newKeySet());
        iocStore.put("CVE", ConcurrentHashMap.newKeySet());
    }
    
    private void initIocTypeWeights() {
        // 初始化IOC类型权重
        iocTypeWeightMap.put("IP", 70);
        iocTypeWeightMap.put("DOMAIN", 60);
        iocTypeWeightMap.put("URL", 65);
        iocTypeWeightMap.put("FILE_HASH", 80);
        iocTypeWeightMap.put("EMAIL", 50);
        iocTypeWeightMap.put("REGEX", 55);
        iocTypeWeightMap.put("CVE", 75);
    }
    
    @Override
    public List<ThreatResult> detect(Object data) {
        List<Object> dataList = Collections.singletonList(data);
        return detectBatch(dataList);
    }
    
    @Override
    public List<ThreatResult> detectBatch(List<Object> dataList) {
        List<ThreatResult> threatResults = new CopyOnWriteArrayList<>();
        
        if (!isAvailable()) {
            log.warn("IOC Matcher is not available, skipping detection");
            return threatResults;
        }
        
        try {
            // 并行处理数据
            dataList.parallelStream().forEach(data -> {
                if (data instanceof NetFlowData) {
                    List<ThreatResult> results = matchNetFlowData((NetFlowData) data);
                    if (!results.isEmpty()) {
                        threatResults.addAll(results);
                    }
                } else if (data instanceof SecurityLogData) {
                    List<ThreatResult> results = matchSecurityLogData((SecurityLogData) data);
                    if (!results.isEmpty()) {
                        threatResults.addAll(results);
                    }
                } else if (data instanceof SystemLogData) {
                    List<ThreatResult> results = matchSystemLogData((SystemLogData) data);
                    if (!results.isEmpty()) {
                        threatResults.addAll(results);
                    }
                } else if (data instanceof ThreatIntelligenceData) {
                    // 更新IOC库
                    updateIocStore((ThreatIntelligenceData) data);
                } else if (data instanceof VulnerabilityData) {
                    // 匹配漏洞数据
                    List<ThreatResult> results = matchVulnerabilityData((VulnerabilityData) data);
                    if (!results.isEmpty()) {
                        threatResults.addAll(results);
                    }
                }
            });
            
        } catch (Exception e) {
            log.error("Error during IOC matching: {}", e.getMessage());
        }
        
        return threatResults;
    }
    
    private List<ThreatResult> matchNetFlowData(NetFlowData netFlowData) {
        List<ThreatResult> results = new ArrayList<>();
        
        // 检查源IP
        String sourceIp = netFlowData.getSourceIp();
        if (matchIp(sourceIp)) {
            ThreatResult result = createThreatResult("IP", sourceIp, 
                                                  "检测到恶意IP地址(源)", netFlowData.getSourceIp(), 
                                                  netFlowData.getDestIp(), 
                                                  convertToTimestamp(netFlowData.getTimestamp()));
            results.add(result);
        }
        
        // 检查目标IP
        String destIp = netFlowData.getDestIp();
        if (matchIp(destIp)) {
            ThreatResult result = createThreatResult("IP", destIp, 
                                                  "检测到恶意IP地址(目标)", netFlowData.getSourceIp(), 
                                                  netFlowData.getDestIp(), 
                                                  convertToTimestamp(netFlowData.getTimestamp()));
            results.add(result);
        }
        
        // 检查其他网络特征
        // 这里可以扩展更多匹配逻辑
        
        return results;
    }
    
    private List<ThreatResult> matchSecurityLogData(SecurityLogData securityLogData) {
        List<ThreatResult> results = new ArrayList<>();
        
        // 检查源IP
        String sourceIp = securityLogData.getSourceIp();
        if (matchIp(sourceIp)) {
            ThreatResult result = createThreatResult("IP", sourceIp, 
                                                  "安全日志中检测到恶意IP地址", securityLogData.getSourceIp(), 
                                                  securityLogData.getDestIp(), securityLogData.getTimestamp());
            results.add(result);
        }
        
        // 检查消息内容中的IOC
        String message = securityLogData.getMessage();
        if (message != null && !message.isEmpty()) {
            // 匹配URL
            List<String> matchedUrls = matchUrls(message);
            for (String url : matchedUrls) {
                ThreatResult result = createThreatResult("URL", url, 
                                                      "安全日志中检测到恶意URL", securityLogData.getSourceIp(), 
                                                      securityLogData.getDestIp(), 
                                                      convertToTimestamp(securityLogData.getTimestamp()));
                results.add(result);
            }
            
            // 匹配域名
            List<String> matchedDomains = matchDomains(message);
            for (String domain : matchedDomains) {
                ThreatResult result = createThreatResult("DOMAIN", domain, 
                                                      "安全日志中检测到恶意域名", securityLogData.getSourceIp(), 
                                                      securityLogData.getDestIp(), 
                                                      convertToTimestamp(securityLogData.getTimestamp()));
                results.add(result);
            }
            
            // 匹配文件哈希
            List<String> matchedHashes = matchFileHashes(message);
            for (String hash : matchedHashes) {
                ThreatResult result = createThreatResult("FILE_HASH", hash, 
                                                      "安全日志中检测到恶意文件哈希", securityLogData.getSourceIp(), 
                                                      securityLogData.getDestIp(), 
                                                      convertToTimestamp(securityLogData.getTimestamp()));
                results.add(result);
            }
            
            // 匹配正则表达式模式
            List<String> matchedRegex = matchRegexPatterns(message);
            for (String pattern : matchedRegex) {
                ThreatResult result = createThreatResult("REGEX", pattern, 
                                                      "安全日志中检测到可疑模式", securityLogData.getSourceIp(), 
                                                      securityLogData.getDestIp(), 
                                                      convertToTimestamp(securityLogData.getTimestamp()));
                results.add(result);
            }
            
            // 匹配CVE
            List<String> matchedCves = matchCves(message);
            for (String cve : matchedCves) {
                ThreatResult result = createThreatResult("CVE", cve, 
                                                      "安全日志中检测到CVE漏洞利用", securityLogData.getSourceIp(), 
                                                      securityLogData.getDestIp(), 
                                                      convertToTimestamp(securityLogData.getTimestamp()));
                results.add(result);
            }
        }
        
        return results;
    }
    
    private List<ThreatResult> matchSystemLogData(SystemLogData systemLogData) {
        List<ThreatResult> results = new ArrayList<>();
        
        // 检查消息内容中的IOC
        String message = systemLogData.getMessage();
        if (message != null && !message.isEmpty()) {
            // 匹配URL
            List<String> matchedUrls = matchUrls(message);
            for (String url : matchedUrls) {
                ThreatResult result = createThreatResult("URL", url, 
                                                      "系统日志中检测到恶意URL", systemLogData.getSourceIp(), 
                                                      systemLogData.getHostname(), 
                                                      convertToTimestamp(systemLogData.getTimestamp()));
                results.add(result);
            }
            
            // 匹配文件哈希
            List<String> matchedHashes = matchFileHashes(message);
            for (String hash : matchedHashes) {
                ThreatResult result = createThreatResult("FILE_HASH", hash, 
                                                      "系统日志中检测到恶意文件哈希", systemLogData.getSourceIp(), 
                                                      systemLogData.getHostname(), 
                                                      convertToTimestamp(systemLogData.getTimestamp()));
                results.add(result);
            }
            
            // 匹配邮箱地址
            List<String> matchedEmails = matchEmails(message);
            for (String email : matchedEmails) {
                ThreatResult result = createThreatResult("EMAIL", email, 
                                                      "系统日志中检测到恶意邮箱地址", systemLogData.getSourceIp(), 
                                                      systemLogData.getHostname(), 
                                                      convertToTimestamp(systemLogData.getTimestamp()));
                results.add(result);
            }
        }
        
        return results;
    }
    
    private List<ThreatResult> matchVulnerabilityData(VulnerabilityData vulnerabilityData) {
        List<ThreatResult> results = new ArrayList<>();
        
        // 检查CVE ID
        String cveId = vulnerabilityData.getCveId();
        if (matchCve(cveId)) {
            ThreatResult result = createThreatResult("CVE", cveId, 
                                                  "检测到资产存在已知漏洞", vulnerabilityData.getAssetIp(), 
                                                  vulnerabilityData.getAssetIp(), System.currentTimeMillis());
            // 根据CVSS分数调整威胁等级
            if (vulnerabilityData.getCvssScore() >= 9.0) {
                result.setThreatLevel("CRITICAL");
                result.setDetectionScore(100.0);
            } else if (vulnerabilityData.getCvssScore() >= 7.0) {
                result.setThreatLevel("HIGH");
                result.setDetectionScore(80.0);
            } else if (vulnerabilityData.getCvssScore() >= 4.0) {
                result.setThreatLevel("MEDIUM");
                result.setDetectionScore(60.0);
            }
            
            results.add(result);
        }
        
        return results;
    }
    
    private boolean matchIp(String ip) {
        return ip != null && !ip.isEmpty() && iocStore.get("IP").contains(ip);
    }
    
    private List<String> matchUrls(String text) {
        List<String> matchedUrls = new ArrayList<>();
        Set<String> urlIocs = iocStore.get("URL");
        
        for (String url : urlIocs) {
            if (text.contains(url)) {
                matchedUrls.add(url);
            }
        }
        
        return matchedUrls;
    }
    
    private List<String> matchDomains(String text) {
        List<String> matchedDomains = new ArrayList<>();
        Set<String> domainIocs = iocStore.get("DOMAIN");
        
        for (String domain : domainIocs) {
            if (text.contains(domain)) {
                matchedDomains.add(domain);
            }
        }
        
        return matchedDomains;
    }
    
    private List<String> matchFileHashes(String text) {
        List<String> matchedHashes = new ArrayList<>();
        Set<String> hashIocs = iocStore.get("FILE_HASH");
        
        for (String hash : hashIocs) {
            if (text.contains(hash)) {
                matchedHashes.add(hash);
            }
        }
        
        return matchedHashes;
    }
    
    private List<String> matchEmails(String text) {
        List<String> matchedEmails = new ArrayList<>();
        Set<String> emailIocs = iocStore.get("EMAIL");
        
        for (String email : emailIocs) {
            if (text.contains(email)) {
                matchedEmails.add(email);
            }
        }
        
        return matchedEmails;
    }
    
    private List<String> matchRegexPatterns(String text) {
        List<String> matchedPatterns = new ArrayList<>();
        Set<String> regexIocs = iocStore.get("REGEX");
        
        for (String patternStr : regexIocs) {
            Pattern pattern = regexPatternCache.computeIfAbsent(patternStr, Pattern::compile);
            if (pattern.matcher(text).find()) {
                matchedPatterns.add(patternStr);
            }
        }
        
        return matchedPatterns;
    }
    
    private List<String> matchCves(String text) {
        List<String> matchedCves = new ArrayList<>();
        Set<String> cveIocs = iocStore.get("CVE");
        
        for (String cve : cveIocs) {
            if (text.contains(cve)) {
                matchedCves.add(cve);
            }
        }
        
        return matchedCves;
    }
    
    private boolean matchCve(String cveId) {
        return cveId != null && !cveId.isEmpty() && iocStore.get("CVE").contains(cveId);
    }
    
    /**
     * 将LocalDateTime转换为时间戳
     */
    private long convertToTimestamp(java.time.LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return System.currentTimeMillis();
        }
        return localDateTime.atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
    
    /**
     * 将Long时间戳转换为时间戳（直接返回）
     */
    private long convertToTimestamp(Long timestamp) {
        if (timestamp == null) {
            return System.currentTimeMillis();
        }
        return timestamp;
    }
    
    private void updateIocStore(ThreatIntelligenceData threatData) {
        String iocType = threatData.getIocType();
        String iocValue = threatData.getIocValue();
        
        if (iocType != null && iocValue != null && !iocValue.isEmpty()) {
            Set<String> iocSet = iocStore.get(iocType.toUpperCase());
            if (iocSet != null) {
                boolean added = iocSet.add(iocValue);
                if (added) {
                    log.debug("Added new IOC: type={}, value={}", iocType, iocValue);
                    
                    // 如果是正则表达式，预编译并缓存
                    if ("REGEX".equals(iocType.toUpperCase())) {
                        try {
                            Pattern pattern = Pattern.compile(iocValue);
                            regexPatternCache.put(iocValue, pattern);
                        } catch (Exception e) {
                            log.error("Invalid regex pattern: {}", iocValue, e);
                            iocSet.remove(iocValue);
                        }
                    }
                }
            }
        }
    }
    
    private ThreatResult createThreatResult(String iocType, String iocValue, 
                                          String description, String source, 
                                          String target, long timestamp) {
        ThreatResult result = new ThreatResult();
        result.setDetectionId("IOC-" + UUID.randomUUID().toString().substring(0, 8));
        result.setTimestamp(timestamp);
        result.setThreatType("IOC_MATCH");
        
        // 根据IOC类型设置威胁等级和分数
        Integer weight = iocTypeWeightMap.get(iocType);
        if (weight != null) {
            if (weight >= 80) {
                result.setThreatLevel("CRITICAL");
            } else if (weight >= 70) {
                result.setThreatLevel("HIGH");
            } else if (weight >= 50) {
                result.setThreatLevel("MEDIUM");
            } else {
                result.setThreatLevel("LOW");
            }
            result.setDetectionScore(weight.doubleValue());
        } else {
            result.setThreatLevel("MEDIUM");
            result.setDetectionScore(50.0);
        }
        
        result.setSourceIp(source);
        result.setDestIp(target);
        result.setDescription(description + " - IOC: " + iocValue);
        result.setDetectorName(getDetectorName());
        result.setRecommendedAction("阻断" + iocType.toLowerCase() + ": " + iocValue);
        
        return result;
    }
    
    @Override
    public void updateDetectionRules(Map<String, Object> updateData) {
        // 处理IOC更新请求
        if (updateData.containsKey("ioc_updates")) {
            Object iocUpdates = updateData.get("ioc_updates");
            if (iocUpdates instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> updates = (List<Map<String, String>>) iocUpdates;
                for (Map<String, String> update : updates) {
                    String type = update.get("type");
                    String value = update.get("value");
                    String action = update.get("action");
                    
                    if (type != null && value != null) {
                        Set<String> iocSet = iocStore.get(type.toUpperCase());
                        if (iocSet != null) {
                            if ("add".equals(action)) {
                                iocSet.add(value);
                                log.info("Added IOC via rule update: {}={}", type, value);
                            } else if ("remove".equals(action)) {
                                iocSet.remove(value);
                                regexPatternCache.remove(value);
                                log.info("Removed IOC via rule update: {}={}", type, value);
                            }
                        }
                    }
                }
            }
        }
        
        // 处理权重更新
        if (updateData.containsKey("weight_updates")) {
            Object weightUpdates = updateData.get("weight_updates");
            if (weightUpdates instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> updates = (Map<String, Integer>) weightUpdates;
                iocTypeWeightMap.putAll(updates);
                log.info("Updated IOC type weights: {}", updates);
            }
        }
    }
    
    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("initialized", initialized.get());
        status.put("running", running.get());
        
        // 添加各类型IOC数量
        Map<String, Integer> iocCounts = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : iocStore.entrySet()) {
            iocCounts.put(entry.getKey(), entry.getValue().size());
        }
        status.put("ioc_counts", iocCounts);
        status.put("total_iocs", iocStore.values().stream().mapToInt(Set::size).sum());
        
        return status;
    }
    
    @Override
    public String getDetectorName() {
        return "IOC Matcher";
    }
    
    @Override
    public void shutdown() {
        if (initialized.getAndSet(false)) {
            running.set(false);
            iocStore.forEach((type, set) -> set.clear());
            regexPatternCache.clear();
            log.info("IOC Matcher shutdown");
        }
    }
    
    @Override
    public boolean isAvailable() {
        return initialized.get() && running.get();
    }
    
    /**
     * 获取指定类型的IOC数量
     */
    public int getIocCountByType(String type) {
        Set<String> iocSet = iocStore.get(type.toUpperCase());
        return iocSet != null ? iocSet.size() : 0;
    }
    
    /**
     * 手动添加IOC
     */
    public boolean addIoc(String type, String value) {
        Set<String> iocSet = iocStore.get(type.toUpperCase());
        if (iocSet != null) {
            return iocSet.add(value);
        }
        return false;
    }
    
    /**
     * 手动移除IOC
     */
    public boolean removeIoc(String type, String value) {
        Set<String> iocSet = iocStore.get(type.toUpperCase());
        if (iocSet != null) {
            boolean removed = iocSet.remove(value);
            if (removed && "REGEX".equals(type.toUpperCase())) {
                regexPatternCache.remove(value);
            }
            return removed;
        }
        return false;
    }
}