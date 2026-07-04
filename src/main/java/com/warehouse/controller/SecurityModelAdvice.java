package com.warehouse.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SecurityModelAdvice {

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        return isFilterDisabledRender(authentication) || AuthController.hasRole(authentication, "ROLE_ADMIN");
    }

    @ModelAttribute("isManage")
    public boolean isManage(Authentication authentication) {
        return isFilterDisabledRender(authentication) || AuthController.hasRole(authentication, "ROLE_MANAGE");
    }

    @ModelAttribute("isStaff")
    public boolean isStaff(Authentication authentication) {
        return isFilterDisabledRender(authentication) || AuthController.hasRole(authentication, "ROLE_STAFF");
    }

    @ModelAttribute("currentUsername")
    public String currentUsername(Authentication authentication) {
        if (!AuthController.isLoggedIn(authentication)) {
            return null;
        }
        return authentication.getName();
    }

    @ModelAttribute("currentRoleLabel")
    public String currentRoleLabel(Authentication authentication) {
        if (AuthController.hasRole(authentication, "ROLE_ADMIN")) {
            return "ADMIN";
        }
        if (AuthController.hasRole(authentication, "ROLE_MANAGE")) {
            return "MANAGE";
        }
        if (AuthController.hasRole(authentication, "ROLE_STAFF")) {
            return "STAFF";
        }
        return "USER";
    }

    private boolean isFilterDisabledRender(Authentication authentication) {
        return authentication == null;
    }
}
