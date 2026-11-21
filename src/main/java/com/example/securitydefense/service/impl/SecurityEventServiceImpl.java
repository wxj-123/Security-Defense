package com.example.securitydefense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.securitydefense.entity.SecurityEvent;
import com.example.securitydefense.mapper.SecurityEventMapper;
import com.example.securitydefense.service.SecurityEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 安全事件服务实现类
 */
@Service
public class SecurityEventServiceImpl extends ServiceImpl<SecurityEventMapper, SecurityEvent> implements SecurityEventService {

    @Autowired
    private SecurityEventMapper securityEventMapper;

    @Override
    public Map<String, Object> getSecurityEvents(Integer page, Integer pageSize, String type, String level) {
        // 创建分页对象
        Page<SecurityEvent> securityEventPage = new Page<>(page, pageSize);

        // 构建查询条件
        QueryWrapper<SecurityEvent> queryWrapper = new QueryWrapper<>();
        
        // 根据事件类型过滤
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq("event_type", type);
        }
        
        // 根据事件级别过滤
        if (level != null && !level.isEmpty()) {
            queryWrapper.eq("event_level", level);
        }
        
        // 按事件时间降序排列
        queryWrapper.orderByDesc("event_time");

        // 执行分页查询
        Page<SecurityEvent> resultPage = securityEventMapper.selectPage(securityEventPage, queryWrapper);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", resultPage.getRecords());
        result.put("total", resultPage.getTotal());
        result.put("page", resultPage.getCurrent());
        result.put("pageSize", resultPage.getSize());

        return result;
    }
}