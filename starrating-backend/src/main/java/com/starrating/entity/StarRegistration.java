package com.starrating.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("star_registration")
public class StarRegistration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String shopCode;
    private String shopName;
    private String targetStar;
    private String quarter;
    private String status; // PENDING, APPROVED, REJECTED
    private String paymentUrl;
    private String canopyUrl;
    private String rejectReason;
    private OffsetDateTime submitTime;
    private OffsetDateTime auditTime;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
}
