package com.starrating.controller;

import com.starrating.dto.AuthenticatedUser;
import com.starrating.dto.LoginResponse;
import com.starrating.entity.Permission;
import com.starrating.entity.Role;
import com.starrating.service.UserService;
import com.starrating.security.SecurityPrincipal;
import com.starrating.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin // 后期详细配置 CORS
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            AuthenticatedUser authenticatedUser = userService.login(username, password);
            if (authenticatedUser == null) {
                throw new RuntimeException("账号或密码错误");
            }
            return buildLoginResponse(authenticatedUser);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("登录异常: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public LoginResponse me(Authentication authentication) {
        SecurityPrincipal principal = (SecurityPrincipal) authentication.getPrincipal();
        AuthenticatedUser authenticatedUser = userService.loadByUsername(principal.getUsername());
        if (authenticatedUser == null) {
            throw new RuntimeException("账号不存在");
        }
        return buildLoginResponse(authenticatedUser);
    }

    private LoginResponse buildLoginResponse(AuthenticatedUser authenticatedUser) {
        LoginResponse response = new LoginResponse();
        response.setToken(jwtUtils.generateToken(authenticatedUser));
        response.setUsername(authenticatedUser.getUser().getUsername());
        response.setUserType(authenticatedUser.getUser().getUserType());
        response.setDisplayName(authenticatedUser.getUser().getDisplayName());
        response.setRole("FACTORY".equals(authenticatedUser.getUser().getUserType()) ? "ADMIN" : "SHOP");
        response.setShopCode(authenticatedUser.getUser().getShopCode());
        response.setShopName(authenticatedUser.getUser().getShopName());
        response.setRoleCodes(authenticatedUser.getRoles().stream().map(Role::getRoleCode).collect(Collectors.toList()));
        response.setPermissionCodes(authenticatedUser.getPermissions().stream().map(Permission::getPermissionCode).collect(Collectors.toList()));
        response.setRedirect("FACTORY".equals(authenticatedUser.getUser().getUserType()) ? "/admin" : "/register");
        return response;
    }
}
