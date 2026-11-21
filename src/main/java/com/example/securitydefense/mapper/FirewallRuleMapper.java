package com.example.securitydefense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.securitydefense.entity.FirewallRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 防火墙规则Mapper接口
 */
@Mapper
public interface FirewallRuleMapper extends BaseMapper<FirewallRule> {
    
}