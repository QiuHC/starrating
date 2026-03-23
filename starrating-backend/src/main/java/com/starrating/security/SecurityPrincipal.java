package com.starrating.security;

import lombok.Data;

import java.util.List;

@Data
public class SecurityPrincipal {
    private Long userId;
    private String username;
    private String userType;
    private String shopCode;
    private List<String> roleCodes;
    private List<String> permissionCodes;
}
