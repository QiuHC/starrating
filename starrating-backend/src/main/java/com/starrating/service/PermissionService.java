package com.starrating.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starrating.entity.Permission;
import com.starrating.mapper.PermissionMapper;
import org.springframework.stereotype.Service;

@Service
public class PermissionService extends ServiceImpl<PermissionMapper, Permission> {
}
