package com.starrating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starrating.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}
