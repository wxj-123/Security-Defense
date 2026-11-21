package com.example.securitydefense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.securitydefense.entity.SecurityPolicy;
import org.apache.ibatis.annotations.Mapper;

/**
 * 安全策略Mapper接口
 */
@Mapper
public interface SecurityPolicyMapper extends BaseMapper<SecurityPolicy> {

}