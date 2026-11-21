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
 * 系统安全日志采集器
 * 支持Windows事件日志和Linux Syslog的采集
 */
@Slf4j
@Component
public class SystemLogCollector implements DataCollector<SystemLogData> {
    
    @Autowired
    private CollectionProperties collectionProperties;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private long lastCollectTime;
    
    @Override
    public void initialize() {
        if (!initialized.getAndSet(true)) {
            try {
                // 初始化系统日志采集器
                log.info("Initializing System Log collector");
                // 这里可以添加实际的系统日志采集器初始化代码
                // 例如：配置Windows事件日志读取或Syslog监听
                
                running.set(true);
                log.info("System Log collector initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize System Log collector: {}", e.getMessage());
                initialized.set(false);
            }
        }
    }
    
    @Override
    public List<SystemLogData> collect() {
        return collectBatch(100); // 默认批量大小
    }
    
    @Override
    public List<SystemLogData> collectBatch(int batchSize) {
        List<SystemLogData> logDataList = new ArrayList<>();
        
        if (!isAvailable()) {
            log.warn("System Log collector is not available, skipping collection");
            return logDataList;
        }
        
        try {
            // 采集系统安全日志
            log.debug("Collecting system logs with batch size: {}", batchSize);
            
            // 这里添加实际的数据采集逻辑
            // 例如：读取Windows事件日志、监听Syslog端口等
            
            // 模拟数据采集
            String[] systemTypes = {"Windows", "Linux"};
            String[] eventSources = {"Security", "System", "Application", "Audit"};
            String[] eventIds = {"4624", "4625", "4648", "4688", "1102"}; // Windows安全事件ID
            String[] logLevels = {"INFO", "WARN", "ERROR", "CRITICAL"};
            
            for (int i = 0; i < batchSize && i < 20; i++) { // 限制模拟数据量
                SystemLogData logData = new SystemLogData();
                String systemType = systemTypes[i % 2];
                logData.setSystemType(systemType);
                logData.setHostname("HOST-" + systemType + "-" + (100 + i));
                logData.setEventSource(eventSources[i % 4]);
                logData.setEventId(systemType.equals("Windows") ? eventIds[i % 5] : "SYS-" + (1000 + i));
                logData.setLogLevel(logLevels[i % 4]);
                
                // 模拟不同类型的系统日志
                if (i % 5 == 0) {
                    logData.setEventType("LOGIN_SUCCESS");
                    logData.setUsername("user" + i);
                    logData.setSourceIp("192.168.1." + (i + 50));
                    logData.setMessage("User login successful: " + logData.getUsername());
                } else if (i % 5 == 1) {
                    logData.setEventType("LOGIN_FAILURE");
                    logData.setUsername("admin");
                    logData.setSourceIp("10.0.1." + (i + 50));
                    logData.setMessage("Failed login attempt for user: " + logData.getUsername());
                } else if (i % 5 == 2) {
                    logData.setEventType("PROCESS_CREATE");
                    logData.setProcessName("cmd.exe" + (i % 2 == 0 ? "/bash" : ""));
                    logData.setUsername("system" + i % 2);
                    logData.setMessage("Process created: " + logData.getProcessName());
                } else if (i % 5 == 3) {
                    logData.setEventType("PRIVILEGE_ESCALATION");
                    logData.setUsername("user" + i);
                    logData.setMessage("Privilege escalation detected for user: " + logData.getUsername());
                } else {
                    logData.setEventType("CONFIG_CHANGE");
                    logData.setUsername("admin");
                    logData.setMessage("System configuration changed by: " + logData.getUsername());
                }
                
                logData.setTimestamp(System.currentTimeMillis());
                
                logDataList.add(logData);
            }
            
            lastCollectTime = System.currentTimeMillis();
            log.debug("Collected {} system log records", logDataList.size());
            
        } catch (Exception e) {
            log.error("Error collecting system log data: {}", e.getMessage());
        }
        
        return logDataList;
    }
    
    @Override
    public void shutdown() {
        if (initialized.getAndSet(false)) {
            running.set(false);
            log.info("System Log collector shutdown");
            // 这里添加资源释放逻辑
        }
    }
    
    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("initialized", initialized.get());
        status.put("running", running.get());
        status.put("lastCollectTime", lastCollectTime);
        status.put("enabled", collectionProperties.isEnableSystemLogCollection());
        return status;
    }
    
    @Override
    public String getCollectorName() {
        return "System Log Collector";
    }
    
    @Override
    public boolean isAvailable() {
        return initialized.get() && running.get() && collectionProperties.isEnableSystemLogCollection();
    }
}