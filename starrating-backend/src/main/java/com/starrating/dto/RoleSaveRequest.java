package com.starrating.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoleSaveRequest {
    private String roleCode;
    private String roleName;
    private String roleScope;
    private String status;
    private List<Long> permissionIds;
}
