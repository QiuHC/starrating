package com.starrating.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static SecurityPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityPrincipal)) {
            return null;
        }
        return (SecurityPrincipal) authentication.getPrincipal();
    }

    public static boolean isShopUser() {
        SecurityPrincipal principal = currentPrincipal();
        return principal != null && "SHOP".equals(principal.getUserType());
    }
}
