package com.example.securitydefense.situation.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.securitydefense.situation.model.entity.ThreatIntelligenceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ThreatIntelligenceRepository extends BaseMapper<ThreatIntelligenceEntity> {
    
    // 自定义查询方法 - 通过XML映射文件实现
    List<ThreatIntelligenceEntity> findByThreatType(@Param("threatType") String threatType);
    
    List<ThreatIntelligenceEntity> findRecentIntelligence(@Param("since") LocalDateTime since);
    
    List<ThreatIntelligenceEntity> findHighThreatIntelligence(@Param("minLevel") int minLevel);
}