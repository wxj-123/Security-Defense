package com.example.securitydefense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.securitydefense.entity.User;
import com.example.securitydefense.dto.UserQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 分页查询用户
     */
    IPage<User> selectUserPage(Page<User> page, @Param("query") UserQueryDTO queryDTO);

    /**
     * 查询用户角色列表
     */
    @Select("SELECT sr.role_code FROM sys_user_role sur " +
            "LEFT JOIN sys_role sr ON sur.role_id = sr.id " +
            "WHERE sur.user_id = #{userId} AND sr.deleted = 0 AND sr.status = 1")
    List<String> selectUserRoles(@Param("userId") Long userId);

    /**
     * 查询用户权限列表
     */
    @Select("SELECT DISTINCT sp.permission_code FROM sys_user_role sur " +
            "LEFT JOIN sys_role_permission srp ON sur.role_id = srp.role_id " +
            "LEFT JOIN sys_permission sp ON srp.permission_id = sp.id " +
            "WHERE sur.user_id = #{userId} AND sp.deleted = 0 AND sp.status = 1")
    List<String> selectUserPermissions(@Param("userId") Long userId);
}
