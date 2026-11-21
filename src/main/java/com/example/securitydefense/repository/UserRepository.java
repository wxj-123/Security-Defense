package com.example.securitydefense.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.securitydefense.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRepository extends BaseMapper<User> {
}