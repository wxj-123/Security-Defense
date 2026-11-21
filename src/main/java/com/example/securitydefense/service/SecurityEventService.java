package com.example.securitydefense.service;

import com.example.securitydefense.entity.SecurityEvent;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;

/**
 * 安全事件服务接口
 */
public interface SecurityEventService extends IService<SecurityEvent> {

    /**
     * 分页查询安全事件
     * @param page 页码
     * @param pageSize 每页大小
     * @param type 事件类型
     * @param level 事件级别
     * @return 事件列表和总数
     */
    Map<String, Object> getSecurityEvents(Integer page, Integer pageSize, String type, String level);
}