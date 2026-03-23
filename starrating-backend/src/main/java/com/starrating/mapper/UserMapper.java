package com.starrating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starrating.entity.Permission;
import com.starrating.entity.Role;
import com.starrating.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT r.id, r.role_code, r.role_name, r.role_scope, r.status, r.is_system, r.create_time, r.update_time " +
            "FROM sys_role r JOIN sys_user_role ur ON ur.role_id = r.id WHERE ur.user_id = #{userId} ORDER BY r.id")
    List<Role> findRolesByUserId(Long userId);

    @Select("SELECT DISTINCT p.id, p.permission_code, p.permission_name, p.permission_group, p.permission_scope, p.create_time, p.update_time " +
            "FROM sys_permission p " +
            "JOIN sys_role_permission rp ON rp.permission_id = p.id " +
            "JOIN sys_user_role ur ON ur.role_id = rp.role_id " +
            "WHERE ur.user_id = #{userId} ORDER BY p.id")
    List<Permission> findPermissionsByUserId(Long userId);
}
