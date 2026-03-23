package com.starrating.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("sys_role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleCode;
    private String roleName;
    private String roleScope;
    private String status;
    private Boolean isSystem;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
}
