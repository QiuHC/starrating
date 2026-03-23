package com.starrating.dto;

import com.starrating.entity.Permission;
import com.starrating.entity.Role;
import com.starrating.entity.User;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class AuthenticatedUser {
    private User user;
    private List<Role> roles;
    private List<Permission> permissions;

    public List<String> getRoleCodes() {
        return roles.stream().map(Role::getRoleCode).collect(Collectors.toList());
    }

    public List<String> getPermissionCodes() {
        return permissions.stream().map(Permission::getPermissionCode).collect(Collectors.toList());
    }
}
