package com.starrating.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoleView {
    private Long id;
    private String roleCode;
    private String roleName;
    private String roleScope;
    private String status;
    private Boolean isSystem;
    private List<Long> permissionIds;
}
