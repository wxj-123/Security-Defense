package com.example.securitydefense.situation.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.securitydefense.situation.model.entity.AssetRiskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AssetRiskRepository extends BaseMapper<AssetRiskEntity> {
    
    // 自定义查询方法 - 通过XML映射文件实现
    List<AssetRiskEntity> findByAssetType(@Param("assetType") String assetType);
    
    List<AssetRiskEntity> findByRiskLevelGreaterThanEqual(@Param("minRiskLevel") int minRiskLevel);
    
    List<AssetRiskEntity> findHighRiskAssets(@Param("minRiskLevel") int minRiskLevel);
    
    List<Map<String, Object>> getAssetRiskStatistics();
}