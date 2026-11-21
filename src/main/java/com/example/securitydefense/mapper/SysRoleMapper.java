package com.example.securitydefense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.securitydefense.entity.SysRole;
import com.example.securitydefense.dto.RoleUpdateDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper接口
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    
    /**
     * 根据用户ID查询角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "LEFT JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
    
    /**
     * 分页查询角色列表
     */
    IPage<SysRole> selectRolePage(Page<SysRole> page, @Param("roleName") String roleName);
    
    /**
     * 检查角色名称是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_role WHERE role_name = #{roleName} AND deleted = 0 AND id != #{id}")
    int checkRoleNameExists(@Param("roleName") String roleName, @Param("id") Long id);
    
    /**
     * 检查角色编码是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_role WHERE role_code = #{roleCode} AND deleted = 0 AND id != #{id}")
    int checkRoleCodeExists(@Param("roleCode") String roleCode, @Param("id") Long id);
}