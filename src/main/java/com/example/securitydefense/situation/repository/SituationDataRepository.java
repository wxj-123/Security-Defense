package com.example.securitydefense.situation.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.securitydefense.situation.model.entity.SituationDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SituationDataRepository extends BaseMapper<SituationDataEntity> {
    
    // 自定义查询方法 - 通过XML映射文件实现
    List<SituationDataEntity> findRecentData(@Param("since") LocalDateTime since);
    
    long countRecentData(@Param("since") LocalDateTime since);
}