package com.starrating.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starrating.dto.AuthenticatedUser;
import com.starrating.entity.Permission;
import com.starrating.entity.Role;
import com.starrating.entity.User;
import com.starrating.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthenticatedUser login(String username, String password) {
        User user = this.query().eq("username", username).one();
        if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return null;
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        return buildAuthenticatedUser(user);
    }

    public AuthenticatedUser loadByUsername(String username) {
        User user = this.query().eq("username", username).one();
        if (user == null) {
            return null;
        }
        return buildAuthenticatedUser(user);
    }

    public String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new RuntimeException("密码不能为空");
        }
        return passwordEncoder.encode(rawPassword);
    }

    private AuthenticatedUser buildAuthenticatedUser(User user) {
        List<Role> roles = this.baseMapper.findRolesByUserId(user.getId());
        List<Permission> permissions = this.baseMapper.findPermissionsByUserId(user.getId());

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUser(user);
        authenticatedUser.setRoles(roles == null ? Collections.emptyList() : roles);
        authenticatedUser.setPermissions(permissions == null ? Collections.emptyList() : permissions);
        return authenticatedUser;
    }
}
