package com.example.securitydefense.situation.collector;

import com.example.securitydefense.situation.config.CollectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 威胁情报数据采集器
 * 从外部威胁源获取威胁情报数据
 */
@Slf4j
@Component
public class ThreatIntelligenceCollector implements DataCollector<ThreatIntelligenceData> {
    
    @Autowired
    private CollectionProperties collectionProperties;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private long lastCollectTime;
    private long lastUpdateTime;
    
    @Override
    public void initialize() {
        if (!initialized.getAndSet(true)) {
            try {
                // 初始化威胁情报采集器
                log.info("Initializing Threat Intelligence collector");
                // 这里可以添加实际的威胁情报采集器初始化代码
                // 例如：配置API密钥、建立连接等
                
                running.set(true);
                log.info("Threat Intelligence collector initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Threat Intelligence collector: {}", e.getMessage());
                initialized.set(false);
            }
        }
    }
    
    @Override
    public List<ThreatIntelligenceData> collect() {
        return collectBatch(100); // 默认批量大小
    }
    
    @Override
    public List<ThreatIntelligenceData> collectBatch(int batchSize) {
        List<ThreatIntelligenceData> threatDataList = new ArrayList<>();
        
        if (!isAvailable()) {
            log.warn("Threat Intelligence collector is not available, skipping collection");
            return threatDataList;
        }
        
        try {
            // 检查是否需要更新威胁情报
            long currentTime = System.currentTimeMillis();
            long updateInterval = collectionProperties.getThreatIntelUpdateInterval() * 60 * 1000; // 转换为毫秒
            
            if (currentTime - lastUpdateTime < updateInterval) {
                log.debug("Threat intelligence is up to date, no need to update yet");
                return threatDataList;
            }
            
            // 采集威胁情报数据
            log.debug("Collecting threat intelligence data with batch size: {}", batchSize);
            
            // 这里添加实际的数据采集逻辑
            // 例如：调用威胁情报API、下载IOC文件等
            
            // 模拟数据采集
            String[] iocTypes = {"IP", "DOMAIN", "URL", "HASH", "EMAIL"};
            String[] threatTypes = {"MALWARE", "PHISHING", "C2", "SPAM", "RANSOMWARE"};
            String[] sources = {"AlienVault", "VirusTotal", "AbuseIPDB", "MISP", "ThreatFox"};
            String[] threatLevels = {"HIGH", "MEDIUM", "LOW"};
            
            for (int i = 0; i < batchSize && i < 25; i++) { // 限制模拟数据量
                ThreatIntelligenceData threatData = new ThreatIntelligenceData();
                String iocType = iocTypes[i % 5];
                threatData.setIocType(iocType);
                
                // 根据不同的IOC类型生成不同的值
                switch (iocType) {
                    case "IP":
                        threatData.setIocValue("103." + (200 + i % 50) + "." + (100 + i % 150) + "." + (i % 255 + 1));
                        break;
                    case "DOMAIN":
                        threatData.setIocValue("malicious-domain-" + i + ".com");
                        break;
                    case "URL":
                        threatData.setIocValue("http://malicious-site-" + i + ".com/download/malware.exe");
                        break;
                    case "HASH":
                        threatData.setIocValue("a1b2c3d4e5f6" + String.format("%024x", i));
                        break;
                    case "EMAIL":
                        threatData.setIocValue("spam-user-" + i + "@malicious-domain.com");
                        break;
                }
                
                threatData.setThreatType(threatTypes[i % 5]);
                threatData.setThreatLevel(threatLevels[i % 3]);
                threatData.setConfidenceScore(70 + (i % 25)); // 70-94的置信度
                threatData.setSource(sources[i % 5]);
                threatData.setDescription("Malicious " + iocType + " associated with " + threatTypes[i % 5]);
                threatData.setFirstSeen(System.currentTimeMillis() - (i * 86400000)); // 每天递增
                threatData.setLastSeen(System.currentTimeMillis() - (i * 3600000)); // 每小时递增
                threatData.setTimestamp(System.currentTimeMillis());
                
                threatDataList.add(threatData);
            }
            
            lastCollectTime = System.currentTimeMillis();
            lastUpdateTime = System.currentTimeMillis();
            log.debug("Collected {} threat intelligence records", threatDataList.size());
            
        } catch (Exception e) {
            log.error("Error collecting threat intelligence data: {}", e.getMessage());
        }
        
        return threatDataList;
    }
    
    @Override
    public void shutdown() {
        if (initialized.getAndSet(false)) {
            running.set(false);
            log.info("Threat Intelligence collector shutdown");
            // 这里添加资源释放逻辑
        }
    }
    
    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("initialized", initialized.get());
        status.put("running", running.get());
        status.put("lastCollectTime", lastCollectTime);
        status.put("lastUpdateTime", lastUpdateTime);
        status.put("enabled", collectionProperties.isEnableThreatIntelligence());
        return status;
    }
    
    @Override
    public String getCollectorName() {
        return "Threat Intelligence Collector";
    }
    
    @Override
    public boolean isAvailable() {
        return initialized.get() && running.get() && collectionProperties.isEnableThreatIntelligence();
    }
}