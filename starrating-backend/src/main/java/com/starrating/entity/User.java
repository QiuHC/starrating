package com.starrating.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String role; // 'ADMIN', 'SHOP'
    private String shopCode;
    private String shopName;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
}
