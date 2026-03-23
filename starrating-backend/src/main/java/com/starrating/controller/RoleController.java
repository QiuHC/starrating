package com.starrating.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.starrating.dto.PermissionGroupView;
import com.starrating.dto.RoleSaveRequest;
import com.starrating.dto.RoleView;
import com.starrating.entity.Permission;
import com.starrating.entity.Role;
import com.starrating.entity.RolePermission;
import com.starrating.service.PermissionService;
import com.starrating.service.RolePermissionService;
import com.starrating.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RolePermissionService rolePermissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('role_manage')")
    public List<RoleView> list() {
        Map<Long, List<Long>> rolePermissionMap = rolePermissionService.list().stream()
                .collect(Collectors.groupingBy(RolePermission::getRoleId,
                        Collectors.mapping(RolePermission::getPermissionId, Collectors.toList())));

        return roleService.lambdaQuery().orderByAsc(Role::getId).list().stream().map(role -> {
            RoleView view = new RoleView();
            view.setId(role.getId());
            view.setRoleCode(role.getRoleCode());
            view.setRoleName(role.getRoleName());
            view.setRoleScope(role.getRoleScope());
            view.setStatus(role.getStatus());
            view.setIsSystem(role.getIsSystem());
            view.setPermissionIds(rolePermissionMap.getOrDefault(role.getId(), List.of()));
            return view;
        }).collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('role_manage')")
    public RoleView create(@RequestBody RoleSaveRequest request) {
        Role role = new Role();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setRoleScope(request.getRoleScope());
        role.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
        role.setIsSystem(Boolean.FALSE);
        role.setCreateTime(OffsetDateTime.now());
        role.setUpdateTime(OffsetDateTime.now());
        roleService.save(role);
        replaceRolePermissions(role.getId(), request.getPermissionIds());
        return buildRoleView(role, request.getPermissionIds());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role_manage')")
    public RoleView update(@PathVariable Long id, @RequestBody RoleSaveRequest request) {
        Role role = roleService.getById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        role.setRoleName(request.getRoleName());
        role.setRoleScope(request.getRoleScope());
        role.setStatus(request.getStatus());
        role.setUpdateTime(OffsetDateTime.now());
        roleService.updateById(role);
        replaceRolePermissions(role.getId(), request.getPermissionIds());
        return buildRoleView(role, request.getPermissionIds());
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('role_manage')")
    public List<PermissionGroupView> permissions() {
        return permissionService.lambdaQuery().orderByAsc(Permission::getId).list().stream()
                .collect(Collectors.groupingBy(Permission::getPermissionGroup))
                .entrySet()
                .stream()
                .map(entry -> {
                    PermissionGroupView view = new PermissionGroupView();
                    view.setGroupName(entry.getKey());
                    view.setPermissions(entry.getValue());
                    return view;
                })
                .collect(Collectors.toList());
    }

    private void replaceRolePermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionService.remove(new QueryWrapper<RolePermission>().eq("role_id", roleId));
        if (permissionIds == null) {
            return;
        }
        permissionIds.forEach(permissionId -> {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermission.setCreateTime(OffsetDateTime.now());
            rolePermissionService.save(rolePermission);
        });
    }

    private RoleView buildRoleView(Role role, List<Long> permissionIds) {
        RoleView view = new RoleView();
        view.setId(role.getId());
        view.setRoleCode(role.getRoleCode());
        view.setRoleName(role.getRoleName());
        view.setRoleScope(role.getRoleScope());
        view.setStatus(role.getStatus());
        view.setIsSystem(role.getIsSystem());
        view.setPermissionIds(permissionIds == null ? List.of() : permissionIds);
        return view;
    }
}
