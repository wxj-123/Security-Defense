package com.example.securitydefense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.securitydefense.entity.SecurityEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 安全事件数据访问层
 */
@Mapper
public interface SecurityEventMapper extends BaseMapper<SecurityEvent> {
}