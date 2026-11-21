package com.example.securitydefense.situation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 态势感知系统核心配置
 */
@Configuration
public class SituationConfig {
    
    @Bean
    @ConfigurationProperties(prefix = "situation.evaluation")
    public EvaluationProperties evaluationProperties() {
        return new EvaluationProperties();
    }
    
    @Bean
    @ConfigurationProperties(prefix = "situation.detection")
    public DetectionProperties detectionProperties() {
        return new DetectionProperties();
    }
    
    @Bean
    @ConfigurationProperties(prefix = "situation.collection")
    public CollectionProperties collectionProperties() {
        return new CollectionProperties();
    }
}