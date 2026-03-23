package com.starrating.dto;

import lombok.Data;

@Data
public class AccountSaveRequest {
    private String username;
    private String email;
    private String displayName;
    private String password;
    private String userType;
    private String status;
    private String shopCode;
    private String shopName;
    private Long roleId;
}
