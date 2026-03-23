package com.starrating.dto;

import lombok.Data;

@Data
public class AccountView {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String userType;
    private String status;
    private String shopCode;
    private String shopName;
    private Long roleId;
    private String roleCode;
    private String roleName;
}
