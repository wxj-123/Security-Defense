package com.example.securitydefense.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.securitydefense.entity.SecurityEvent;
import com.example.securitydefense.mapper.SecurityEventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Random;

/**
 * 安全事件定时任务
 * 用于定期生成新的安全事件，模拟实时数据更新
 */
@Slf4j
@Component
public class SecurityEventScheduler {

    @Autowired
    private SecurityEventMapper securityEventMapper;

    private final Random random = new Random();

    /**
     * 每30秒生成一个新的安全事件
     */
    @Scheduled(fixedRate = 30000)
    public void generateRandomSecurityEvent() {
        try {
            // 检查数据库中是否已有事件
            LambdaQueryWrapper<SecurityEvent> countQuery = new LambdaQueryWrapper<>();
            Long count = securityEventMapper.selectCount(countQuery);

            // 如果事件数量过多，删除最旧的事件
            if (count > 50) {
                LambdaQueryWrapper<SecurityEvent> deleteQuery = new LambdaQueryWrapper<>();
                deleteQuery.orderByAsc(SecurityEvent::getEventTime);
                deleteQuery.last("LIMIT 1");
                securityEventMapper.delete(deleteQuery);
                log.info("已删除最旧的安全事件");
            }

            // 生成新的安全事件
            SecurityEvent event = createRandomEvent();
            securityEventMapper.insert(event);
            log.info("生成新安全事件: {}", event.getEventType());

        } catch (Exception e) {
            log.error("生成安全事件失败", e);
        }
    }

    /**
     * 创建随机安全事件
     */
    private SecurityEvent createRandomEvent() {
        SecurityEvent event = new SecurityEvent();

        // 随机事件类型
        String[] eventTypes = {"PORT_SCAN", "BRUTE_FORCE", "DDOS", "MALWARE", "SQL_INJECTION", "XSS"};
        event.setEventType(eventTypes[random.nextInt(eventTypes.length)]);

        // 随机事件等级
        String[] eventLevels = {"CRITICAL", "HIGH", "WARNING", "INFO"};
        event.setEventLevel(eventLevels[random.nextInt(eventLevels.length)]);

        // 随机IP地址
        event.setSourceIp(generateRandomIP());
        event.setTargetIp("10.0.0." + random.nextInt(256));

        // 随机端口
        event.setSourcePort(random.nextInt(65535));
        event.setTargetPort(random.nextInt(100) + 22);

        // 随机协议
        String[] protocols = {"TCP", "UDP", "HTTP", "HTTPS"};
        event.setProtocol(protocols[random.nextInt(protocols.length)]);

        // 事件描述
        event.setDescription(generateEventDescription(event.getEventType()));

        // 随机状态
        String[] statuses = {"PENDING", "PROCESSING", "RESOLVED", "IGNORED"};
        event.setStatus(statuses[random.nextInt(statuses.length)]);

        // 事件时间 - 使用当前时间，确保是实时更新的
        event.setEventTime(new Date());
        event.setCreateTime(new Date());

        return event;
    }

    /**
     * 生成随机IP地址
     */
    private String generateRandomIP() {
        return String.format("%d.%d.%d.%d",
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        );
    }

    /**
     * 生成事件描述
     */
    private String generateEventDescription(String eventType) {
        switch (eventType) {
            case "PORT_SCAN":
                return "检测到端口扫描行为，已记录详细日志。\n" +
                       "扫描端口范围: 1-65535\n" +
                       "扫描方式: SYN扫描\n" +
                       "检测到的开放端口: [22, 80, 443, 3306, 3389]";
            case "BRUTE_FORCE":
                return "SSH暴力破解尝试，已触发告警。\n" +
                       "尝试的账户名: [admin, root, user, test]\n" +
                       "失败次数: 15次\n" +
                       "攻击特征: 字典攻击 + 暴力破解";
            case "DDOS":
                return "检测到DDoS攻击流量，已启动防护机制。\n" +
                       "攻击类型: SYN Flood\n" +
                       "峰值流量: 12.5 Gbps\n" +
                       "源IP数量: 1524个\n" +
                       "攻击持续时间: 3600秒";
            case "MALWARE":
                return "发现恶意软件通信，已隔离相关流量。\n" +
                       "恶意软件名称: TrickBot\n" +
                       "C2服务器: 192.168.100.45\n" +
                       "通信协议: HTTPS/TCP\n" +
                       "检测特征: 域名生成算法(DGA)";
            case "SQL_INJECTION":
                return "检测到SQL注入尝试，已拦截请求。\n" +
                       "注入点: /api/user/login\n" +
                       "攻击Payload: SELECT * FROM users WHERE id = '1' OR '1'='1'\n" +
                       "注入类型: Union-based SQL Injection\n" +
                       "WAF拦截: 已拦截并记录";
            case "XSS":
                return "发现跨站脚本攻击，已净化输入。\n" +
                       "攻击向量: Reflected XSS\n" +
                       "注入点: 搜索框参数\n" +
                       "攻击Payload: <script>alert('XSS')</script>\n" +
                       "防护措施: HTML实体编码 + CSP";
            default:
                return "检测到可疑活动，需要进一步分析。\n" +
                       "事件类型: " + eventType + "\n" +
                       "建议操作: 检查系统日志，分析流量特征";
        }
    }
}
