package com.starrating.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starrating.entity.Role;
import com.starrating.mapper.RoleMapper;
import org.springframework.stereotype.Service;

@Service
public class RoleService extends ServiceImpl<RoleMapper, Role> {
}
