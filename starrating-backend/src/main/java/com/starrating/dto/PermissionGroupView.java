package com.starrating.dto;

import com.starrating.entity.Permission;
import lombok.Data;

import java.util.List;

@Data
public class PermissionGroupView {
    private String groupName;
    private List<Permission> permissions;
}
