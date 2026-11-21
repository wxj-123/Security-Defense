package com.example.securitydefense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.securitydefense.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统配置Mapper接口
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
    
    /**
     * 根据配置键查询配置
     */
    @Select("SELECT * FROM sys_config WHERE config_key = #{configKey}")
    SysConfig selectByConfigKey(@Param("configKey") String configKey);
    
    /**
     * 根据配置类型查询配置列表
     */
    @Select("SELECT * FROM sys_config WHERE config_type = #{configType}")
    List<SysConfig> selectByConfigType(@Param("configType") String configType);
}