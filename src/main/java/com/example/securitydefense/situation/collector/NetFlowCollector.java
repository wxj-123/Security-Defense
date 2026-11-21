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
 * 网络流量数据采集器
 * 支持NetFlow/sFlow协议数据采集
 */
@Slf4j
@Component
public class NetFlowCollector implements DataCollector<NetFlowData> {
    
    @Autowired
    private CollectionProperties collectionProperties;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private long lastCollectTime;
    
    @Override
    public void initialize() {
        if (!initialized.getAndSet(true)) {
            try {
                // 初始化NetFlow采集器
                log.info("Initializing NetFlow collector");
                // 这里可以添加实际的NetFlow采集器初始化代码
                // 例如：配置监听端口、设置模板等
                
                running.set(true);
                log.info("NetFlow collector initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize NetFlow collector: {}", e.getMessage());
                initialized.set(false);
            }
        }
    }
    
    @Override
    public List<NetFlowData> collect() {
        return collectBatch(100); // 默认批量大小
    }
    
    @Override
    public List<NetFlowData> collectBatch(int batchSize) {
        List<NetFlowData> flowDataList = new ArrayList<>();
        
        if (!isAvailable()) {
            log.warn("NetFlow collector is not available, skipping collection");
            return flowDataList;
        }
        
        try {
            // 采集NetFlow数据
            log.debug("Collecting NetFlow data with batch size: {}", batchSize);
            
            // 这里添加实际的数据采集逻辑
            // 例如：从网络设备接收NetFlow数据包并解析
            
            // 模拟数据采集
            for (int i = 0; i < batchSize && i < 10; i++) { // 限制模拟数据量
                NetFlowData flowData = new NetFlowData();
                flowData.setSourceIp("192.168.1." + (i + 10));
                flowData.setDestIp("10.0.0." + (i + 1));
                flowData.setSourcePort(10000 + i);
                flowData.setDestPort(80 + i % 3); // 80, 81, 82
                flowData.setProtocol(i % 2 == 0 ? "TCP" : "UDP");
                flowData.setPacketCount((long) (100 + i * 10));
                flowData.setByteCount((long) (1024 * (100 + i * 10)));
                flowData.setDuration((double) (60 + i));
                flowData.setTimestamp(java.time.LocalDateTime.now());
                
                flowDataList.add(flowData);
            }
            
            lastCollectTime = System.currentTimeMillis();
            log.debug("Collected {} NetFlow records", flowDataList.size());
            
        } catch (Exception e) {
            log.error("Error collecting NetFlow data: {}", e.getMessage());
        }
        
        return flowDataList;
    }
    
    @Override
    public void shutdown() {
        if (initialized.getAndSet(false)) {
            running.set(false);
            log.info("NetFlow collector shutdown");
            // 这里添加资源释放逻辑
        }
    }
    
    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("initialized", initialized.get());
        status.put("running", running.get());
        status.put("lastCollectTime", lastCollectTime);
        status.put("enabled", collectionProperties.isEnableNetflowCollection());
        return status;
    }
    
    @Override
    public String getCollectorName() {
        return "NetFlow Collector";
    }
    
    @Override
    public boolean isAvailable() {
        return initialized.get() && running.get() && collectionProperties.isEnableNetflowCollection();
    }
}