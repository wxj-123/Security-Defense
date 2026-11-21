package com.example.securitydefense.situation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 权重配置属性
 */
@Data
@ConfigurationProperties(prefix = "situation.evaluation.weights")
public class Weights {
    
    /**
     * 资产类型权重映射
     */
    private Map<String, Double> assetTypeWeights = new HashMap<String, Double>() {{
        put("SERVER", 1.5);
        put("DATABASE", 1.8);
        put("NETWORK", 1.3);
        put("APPLICATION", 1.6);
        put("WORKSTATION", 1.0);
        put("MOBILE", 1.2);
        put("IOT", 1.1);
    }};
    
    /**
     * 漏洞类型权重映射
     */
    private Map<String, Double> vulnerabilityTypeWeights = new HashMap<String, Double>() {{
        put("CRITICAL", 10.0);
        put("HIGH", 8.0);
        put("MEDIUM", 5.0);
        put("LOW", 2.0);
    }};
    
    /**
     * 威胁类型权重映射
     */
    private Map<String, Double> threatTypeWeights = new HashMap<String, Double>() {{
        put("APT", 1.5);
        put("RANSOMWARE", 1.4);
        put("DATA_BREACH", 1.3);
        put("SQL_INJECTION", 1.2);
        put("XSS", 1.1);
        put("CSRF", 1.0);
        put("MALWARE", 1.3);
        put("PHISHING", 1.2);
        put("DDoS", 1.0);
        put("BRUTE_FORCE", 0.9);
        put("PRIVILEGE_ESCALATION", 1.3);
        put("LATERAL_MOVEMENT", 1.2);
        put("DATA_EXFILTRATION", 1.4);
    }};
}