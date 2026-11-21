package com.example.securitydefense.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.securitydefense.entity.SysBackup;

import java.util.List;

/**
 * 数据备份服务接口
 */
public interface BackupService extends IService<SysBackup> {
    
    /**
     * 创建备份
     */
    boolean createBackup(String backupName, String backupType, String operator, String description);
    
    /**
     * 分页查询备份列表
     */
    IPage<SysBackup> queryBackupPage(Integer current, Integer size);
    
    /**
     * 恢复备份
     */
    boolean restoreBackup(Long backupId, String operator);
    
    /**
     * 删除备份
     */
    boolean deleteBackup(Long backupId, String operator);
    
    /**
     * 清理过期备份
     */
    void cleanExpiredBackups(int retentionDays);
}