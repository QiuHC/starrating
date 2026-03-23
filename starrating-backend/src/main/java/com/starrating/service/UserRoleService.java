package com.starrating.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starrating.entity.UserRole;
import com.starrating.mapper.UserRoleMapper;
import org.springframework.stereotype.Service;

@Service
public class UserRoleService extends ServiceImpl<UserRoleMapper, UserRole> {
}
