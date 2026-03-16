package com.starrating.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@TableName(value = "star_registration", autoResultMap = true)
public class StarRegistration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String shopCode;
    private String shopName;
    private Integer targetStar;
    private String quarter;
    private String status; // PENDING, APPROVED, REJECTED
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> evidenceUrls;
    
    private String rejectReason;
    private OffsetDateTime submitTime;
    private OffsetDateTime auditTime;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
}
