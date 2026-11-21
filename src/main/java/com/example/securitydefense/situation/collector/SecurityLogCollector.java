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
 * 安全设备日志采集器
 * 支持防火墙、WAF、IDS/IPS等设备的日志采集
 */
@Slf4j
@Component
public class SecurityLogCollector implements DataCollector<SecurityLogData> {
    
    @Autowired
    private CollectionProperties collectionProperties;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private long lastCollectTime;
    
    @Override
    public void initialize() {
        if (!initialized.getAndSet(true)) {
            try {
                // 初始化安全日志采集器
                log.info("Initializing Security Log collector");
                // 这里可以添加实际的日志采集器初始化代码
                // 例如：配置日志源、建立连接等
                
                running.set(true);
                log.info("Security Log collector initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Security Log collector: {}", e.getMessage());
                initialized.set(false);
            }
        }
    }
    
    @Override
    public List<SecurityLogData> collect() {
        return collectBatch(100); // 默认批量大小
    }
    
    @Override
    public List<SecurityLogData> collectBatch(int batchSize) {
        List<SecurityLogData> logDataList = new ArrayList<>();
        
        if (!isAvailable()) {
            log.warn("Security Log collector is not available, skipping collection");
            return logDataList;
        }
        
        try {
            // 采集安全设备日志
            log.debug("Collecting security logs with batch size: {}", batchSize);
            
            // 这里添加实际的数据采集逻辑
            // 例如：从安全设备获取日志、通过syslog接收等
            
            // 模拟数据采集
            String[] deviceTypes = {"Firewall", "WAF", "IDS", "IPS"};
            String[] logLevels = {"INFO", "WARN", "ERROR", "CRITICAL"};
            String[] eventTypes = {"BLOCK", "ALERT", "DROP", "ALLOW"};
            
            for (int i = 0; i < batchSize && i < 15; i++) { // 限制模拟数据量
                SecurityLogData logData = new SecurityLogData();
                logData.setDeviceId("SEC-" + deviceTypes[i % 4] + "-" + (100 + i));
                logData.setDeviceType(deviceTypes[i % 4]);
                logData.setLogLevel(logLevels[i % 4]);
                logData.setEventId("EVT-" + (20240000 + i));
                logData.setEventType(eventTypes[i % 4]);
                logData.setSourceIp("192.168.1." + (i + 20));
                logData.setDestIp("10.0.0." + (i + 10));
                logData.setSourcePort(50000 + i);
                logData.setDestPort(80 + i % 10);
                logData.setProtocol(i % 2 == 0 ? "TCP" : "UDP");
                logData.setMessage("Security event detected: " + eventTypes[i % 4] + " from " + logData.getSourceIp());
                logData.setTimestamp(System.currentTimeMillis());
                
                logDataList.add(logData);
            }
            
            lastCollectTime = System.currentTimeMillis();
            log.debug("Collected {} security log records", logDataList.size());
            
        } catch (Exception e) {
            log.error("Error collecting security log data: {}", e.getMessage());
        }
        
        return logDataList;
    }
    
    @Override
    public void shutdown() {
        if (initialized.getAndSet(false)) {
            running.set(false);
            log.info("Security Log collector shutdown");
            // 这里添加资源释放逻辑
        }
    }
    
    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("initialized", initialized.get());
        status.put("running", running.get());
        status.put("lastCollectTime", lastCollectTime);
        status.put("enabled", collectionProperties.isEnableSecurityLogCollection());
        return status;
    }
    
    @Override
    public String getCollectorName() {
        return "Security Log Collector";
    }
    
    @Override
    public boolean isAvailable() {
        return initialized.get() && running.get() && collectionProperties.isEnableSecurityLogCollection();
    }
}