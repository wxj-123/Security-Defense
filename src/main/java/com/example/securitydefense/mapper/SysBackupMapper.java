package com.example.securitydefense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.securitydefense.entity.SysBackup;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据备份Mapper接口
 */
@Mapper
public interface SysBackupMapper extends BaseMapper<SysBackup> {
    
    // 基本CRUD操作已由BaseMapper提供
}