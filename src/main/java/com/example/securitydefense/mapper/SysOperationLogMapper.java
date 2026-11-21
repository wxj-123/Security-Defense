package com.example.securitydefense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.securitydefense.entity.SysOperationLog;
import com.example.securitydefense.dto.OperationLogQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 操作日志Mapper接口
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {
    
    /**
     * 分页查询操作日志
     */
    IPage<SysOperationLog> selectOperationLogPage(Page<SysOperationLog> page, @Param("query") OperationLogQueryDTO query);
}