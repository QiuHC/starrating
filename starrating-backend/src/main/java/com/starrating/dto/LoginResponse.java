package com.starrating.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String role;      // 'ADMIN' 或 'SHOP'
    private String userType;
    private String displayName;
    private String shopCode;  // 仅针对 'SHOP' 角色
    private String shopName;  // 仅针对 'SHOP' 角色
    private List<String> roleCodes;
    private List<String> permissionCodes;
    private String redirect;  // 建议跳转的路径 (例如: '/admin' 或 '/register')
}
