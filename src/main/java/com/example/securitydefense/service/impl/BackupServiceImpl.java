package com.example.securitydefense.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.securitydefense.entity.SysBackup;
import com.example.securitydefense.mapper.SysBackupMapper;
import com.example.securitydefense.service.BackupService;
import com.example.securitydefense.service.OperationLogService;
import com.example.securitydefense.service.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 数据备份服务实现类
 */
@Slf4j
@Service
public class BackupServiceImpl extends ServiceImpl<SysBackupMapper, SysBackup> implements BackupService {
    
    @Autowired
    private SysBackupMapper backupMapper;
    
    @Autowired
    private OperationLogService operationLogService;
    
    @Autowired
    private SystemService systemService;
    
    @Value("${backup.path:/data/backups}")
    private String backupPath;
    
    @Override
    @Transactional
    public boolean createBackup(String backupName, String backupType, String operator, String description) {
        try {
            // 创建备份目录
            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }
            
            // 生成备份文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_%s.sql", backupName, timestamp);
            String filePath = Paths.get(backupPath, fileName).toString();
            
            // 执行数据库备份（这里简化处理，实际应用中可以使用mysqldump工具或Spring Boot Admin等）
            boolean backupResult = executeDatabaseBackup(filePath);
            
            if (backupResult) {
                // 获取备份文件大小
                File backupFile = new File(filePath);
                long fileSize = backupFile.exists() ? backupFile.length() : 0;
                
                // 保存备份记录
                SysBackup backup = new SysBackup();
                backup.setBackupName(backupName);
                backup.setBackupPath(filePath);
                backup.setBackupType(backupType);
                backup.setBackupSize(fileSize);
                backup.setBackupStatus("成功");
                backup.setBackupUser(operator);
                backup.setDescription(description);
                backup.setCreateTime(LocalDateTime.now());
                
                int result = backupMapper.insert(backup);
                
                if (result > 0) {
                    // 记录操作日志
                    operationLogService.logOperation(
                        operator,
                        "数据备份",
                        "创建",
                        String.format("创建备份 [%s]，文件大小 %d 字节", backupName, fileSize),
                        "成功",
                        null
                    );
                    
                    return true;
                } else {
                    // 备份文件创建成功，但数据库记录失败，删除备份文件
                    try {
                        Files.deleteIfExists(Paths.get(filePath));
                    } catch (IOException e) {
                        log.error("删除备份文件失败", e);
                    }
                    return false;
                }
            } else {
                // 记录失败日志
                operationLogService.logOperation(
                    operator,
                    "数据备份",
                    "创建",
                    String.format("创建备份 [%s] 失败", backupName),
                    "失败",
                    "数据库备份执行失败"
                );
                return false;
            }
            
        } catch (Exception e) {
            log.error("创建备份失败", e);
            
            // 记录失败日志
            operationLogService.logOperation(
                operator,
                "数据备份",
                "创建",
                String.format("创建备份 [%s] 失败", backupName),
                "失败",
                e.getMessage()
            );
            
            return false;
        }
    }
    
    @Override
    public IPage<SysBackup> queryBackupPage(Integer current, Integer size) {
        Page<SysBackup> page = new Page<>(current, size);
        return backupMapper.selectPage(page, null);
    }
    
    @Override
    @Transactional
    public boolean restoreBackup(Long backupId, String operator) {
        try {
            SysBackup backup = getById(backupId);
            if (backup == null) {
                throw new RuntimeException("备份记录不存在");
            }
            
            String backupFilePath = backup.getBackupPath();
            File backupFile = new File(backupFilePath);
            
            if (!backupFile.exists()) {
                throw new RuntimeException("备份文件不存在");
            }
            
            // 执行数据库恢复（这里简化处理，实际应用中需要谨慎处理）
            boolean restoreResult = executeDatabaseRestore(backupFilePath);
            
            if (restoreResult) {
                // 记录操作日志
                operationLogService.logOperation(
                    operator,
                    "数据恢复",
                    "恢复",
                    String.format("从备份 [%s] 恢复数据", backup.getBackupName()),
                    "成功",
                    null
                );
                
                return true;
            } else {
                // 记录失败日志
                operationLogService.logOperation(
                    operator,
                    "数据恢复",
                    "恢复",
                    String.format("从备份 [%s] 恢复数据失败", backup.getBackupName()),
                    "失败",
                    "数据库恢复执行失败"
                );
                
                return false;
            }
            
        } catch (Exception e) {
            log.error("恢复备份失败", e);
            
            // 记录失败日志
            operationLogService.logOperation(
                operator,
                "数据恢复",
                "恢复",
                String.format("恢复备份失败，ID: %d", backupId),
                "失败",
                e.getMessage()
            );
            
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteBackup(Long backupId, String operator) {
        try {
            SysBackup backup = getById(backupId);
            if (backup == null) {
                throw new RuntimeException("备份记录不存在");
            }
            
            // 删除备份文件
            String backupFilePath = backup.getBackupPath();
            File backupFile = new File(backupFilePath);
            if (backupFile.exists()) {
                backupFile.delete();
            }
            
            // 删除数据库记录
            boolean result = removeById(backupId);
            
            if (result) {
                // 记录操作日志
                operationLogService.logOperation(
                    operator,
                    "数据备份",
                    "删除",
                    String.format("删除备份 [%s]", backup.getBackupName()),
                    "成功",
                    null
                );
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("删除备份失败", e);
            
            // 记录失败日志
            operationLogService.logOperation(
                operator,
                "数据备份",
                "删除",
                String.format("删除备份失败，ID: %d", backupId),
                "失败",
                e.getMessage()
            );
            
            return false;
        }
    }
    
    @Override
    @Async
    public void cleanExpiredBackups(int retentionDays) {
        try {
            // 计算过期时间
            LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);
            
            // 查询过期备份记录
            List<SysBackup> expiredBackups = backupMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysBackup>()
                    .lt("create_time", expireTime)
            );
            
            int deletedCount = 0;
            for (SysBackup backup : expiredBackups) {
                try {
                    // 删除备份文件
                    File backupFile = new File(backup.getBackupPath());
                    if (backupFile.exists()) {
                        backupFile.delete();
                    }
                    
                    // 删除数据库记录
                    backupMapper.deleteById(backup.getId());
                    deletedCount++;
                } catch (Exception e) {
                    log.error("清理过期备份失败，备份ID: {}", backup.getId(), e);
                }
            }
            
            log.info("清理过期备份完成，删除了 {} 个备份文件", deletedCount);
            
            // 记录清理操作
            operationLogService.logOperation(
                "系统",
                "数据备份",
                "清理",
                String.format("清理了 %d 天前的备份，删除了 %d 个文件", retentionDays, deletedCount),
                "成功",
                null
            );
            
        } catch (Exception e) {
            log.error("清理过期备份失败", e);
        }
    }
    
    /**
     * 执行数据库备份
     */
    private boolean executeDatabaseBackup(String filePath) {
        // 这里简化处理，实际应用中可以使用mysqldump工具
        // 例如：
        // String command = String.format("mysqldump -h%s -u%s -p%s %s > %s", 
        //     dbHost, dbUser, dbPassword, dbName, filePath);
        // 执行命令并返回结果
        
        try {
            // 模拟备份过程
            Path backupFile = Paths.get(filePath);
            String content = "-- Database Backup\n-- Created at " + LocalDateTime.now() + "\n";
            Files.write(backupFile, content.getBytes());
            return true;
        } catch (IOException e) {
            log.error("执行数据库备份失败", e);
            return false;
        }
    }
    
    /**
     * 执行数据库恢复
     */
    private boolean executeDatabaseRestore(String filePath) {
        // 这里简化处理，实际应用中可以使用mysql命令行工具
        // 例如：
        // String command = String.format("mysql -h%s -u%s -p%s %s < %s", 
        //     dbHost, dbUser, dbPassword, dbName, filePath);
        // 执行命令并返回结果
        
        try {
            // 模拟恢复过程
            File backupFile = new File(filePath);
            if (backupFile.exists() && backupFile.length() > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("执行数据库恢复失败", e);
            return false;
        }
    }
}