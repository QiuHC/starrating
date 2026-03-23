package com.starrating.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("sys_permission")
public class Permission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String permissionCode;
    private String permissionName;
    private String permissionGroup;
    private String permissionScope;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
}
