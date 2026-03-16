package com.starrating.controller;

import com.starrating.dto.LoginResponse;
import com.starrating.entity.User;
import com.starrating.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin // 后期详细配置 CORS
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            User user = userService.login(username, password);
            if (user == null) {
                throw new RuntimeException("账号或密码错误");
            }

            LoginResponse response = new LoginResponse();
            response.setToken("mock-jwt-token-" + user.getId()); 
            response.setUsername(user.getUsername());
            response.setRole(user.getRole());
            response.setShopCode(user.getShopCode());
            response.setShopName(user.getShopName());

            // 根据角色设置跳转建议
            if ("ADMIN".equals(user.getRole())) {
                response.setRedirect("/admin");
            } else {
                response.setRedirect("/register");
            }

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("登录异常: " + e.getMessage());
        }
    }
}
