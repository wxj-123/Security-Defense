package com.example.securitydefense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.securitydefense.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    
    /**
     * 根据用户ID查询权限列表
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "LEFT JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "LEFT JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0 AND p.status = 1")
    List<SysPermission> selectPermissionsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查询权限列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "LEFT JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0")
    List<SysPermission> selectPermissionsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询菜单权限树形结构
     */
    @Select("SELECT * FROM sys_permission WHERE deleted = 0 AND status = 1 ORDER BY sort ASC")
    List<SysPermission> selectPermissionTree();
}