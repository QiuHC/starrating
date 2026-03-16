package com.starrating.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String role;      // 'ADMIN' 或 'SHOP'
    private String shopCode;  // 仅针对 'SHOP' 角色
    private String shopName;  // 仅针对 'SHOP' 角色
    private String redirect;  // 建议跳转的路径 (例如: '/admin' 或 '/register')
}
