package com.starrating.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.starrating.dto.AccountSaveRequest;
import com.starrating.dto.AccountView;
import com.starrating.entity.Role;
import com.starrating.entity.User;
import com.starrating.entity.UserRole;
import com.starrating.service.RoleService;
import com.starrating.service.UserRoleService;
import com.starrating.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin
public class AccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRoleService userRoleService;

    @GetMapping
    @PreAuthorize("hasAuthority('account_manage')")
    public List<AccountView> list() {
        List<User> users = userService.lambdaQuery().orderByDesc(User::getCreateTime).list();
        Map<Long, Role> roleMap = roleService.list().stream().collect(Collectors.toMap(Role::getId, role -> role));
        Map<Long, UserRole> userRoleMap = userRoleService.list().stream()
                .collect(Collectors.toMap(UserRole::getUserId, userRole -> userRole, (first, second) -> first, HashMap::new));

        return users.stream().map(user -> {
            AccountView view = new AccountView();
            view.setId(user.getId());
            view.setUsername(user.getUsername());
            view.setEmail(user.getEmail());
            view.setDisplayName(user.getDisplayName());
            view.setUserType(user.getUserType());
            view.setStatus(user.getStatus());
            view.setShopCode(user.getShopCode());
            view.setShopName(user.getShopName());
            UserRole userRole = userRoleMap.get(user.getId());
            if (userRole != null) {
                Role role = roleMap.get(userRole.getRoleId());
                if (role != null) {
                    view.setRoleId(role.getId());
                    view.setRoleCode(role.getRoleCode());
                    view.setRoleName(role.getRoleName());
                }
            }
            return view;
        }).collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('account_manage')")
    public AccountView create(@RequestBody AccountSaveRequest request) {
        Role role = roleService.getById(request.getRoleId());
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        validateUserRoleScope(request.getUserType(), role.getRoleScope());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        user.setUserType(request.getUserType());
        user.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
        user.setShopCode(request.getShopCode());
        user.setShopName(request.getShopName());
        user.setPassword(userService.encodePassword(request.getPassword()));
        user.setCreateTime(OffsetDateTime.now());
        user.setUpdateTime(OffsetDateTime.now());
        normalizeShopUser(user);
        userService.save(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRole.setCreateTime(OffsetDateTime.now());
        userRoleService.save(userRole);
        return buildAccountView(user, role);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('account_manage')")
    public AccountView update(@PathVariable Long id, @RequestBody AccountSaveRequest request) {
        User user = userService.getById(id);
        if (user == null) {
            throw new RuntimeException("账号不存在");
        }
        Role role = roleService.getById(request.getRoleId());
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        validateUserRoleScope(request.getUserType(), role.getRoleScope());

        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        user.setUserType(request.getUserType());
        user.setStatus(request.getStatus());
        user.setShopCode(request.getShopCode());
        user.setShopName(request.getShopName());
        user.setUpdateTime(OffsetDateTime.now());
        normalizeShopUser(user);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(userService.encodePassword(request.getPassword()));
        }
        userService.updateById(user);

        userRoleService.remove(new QueryWrapper<UserRole>().eq("user_id", id));
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRole.setCreateTime(OffsetDateTime.now());
        userRoleService.save(userRole);
        return buildAccountView(user, role);
    }

    private void validateUserRoleScope(String userType, String roleScope) {
        if (userType == null || roleScope == null || !userType.equals(roleScope)) {
            throw new RuntimeException("账号类型与角色域不匹配");
        }
    }

    private void normalizeShopUser(User user) {
        if ("SHOP".equals(user.getUserType())) {
            if (user.getShopCode() == null || user.getShopCode().isBlank()) {
                throw new RuntimeException("店端账号必须绑定 shopCode");
            }
            user.setUsername(user.getShopCode());
        } else {
            user.setShopCode(null);
            user.setShopName(null);
        }
    }

    private AccountView buildAccountView(User user, Role role) {
        AccountView view = new AccountView();
        view.setId(user.getId());
        view.setUsername(user.getUsername());
        view.setEmail(user.getEmail());
        view.setDisplayName(user.getDisplayName());
        view.setUserType(user.getUserType());
        view.setStatus(user.getStatus());
        view.setShopCode(user.getShopCode());
        view.setShopName(user.getShopName());
        view.setRoleId(role.getId());
        view.setRoleCode(role.getRoleCode());
        view.setRoleName(role.getRoleName());
        return view;
    }
}
