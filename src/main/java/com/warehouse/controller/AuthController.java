package com.warehouse.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(Authentication authentication, Model model) {
        if (isLoggedIn(authentication)) {
            return "redirect:" + resolveLandingPage(authentication);
        }
        model.addAttribute(
                "demoNotice",
                "Public demo shows Staff credentials only. Admin and Manager accounts remain hidden from this page."
        );
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return "access-denied";
    }

    static String resolveLandingPage(Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return "/dashboard";
        }
        if (hasRole(authentication, "ROLE_MANAGE")) {
            return "/manager/robot-tasks";
        }
        return "/staff/pickup-request";
    }

    static boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    static boolean hasRole(Authentication authentication, String roleName) {
        if (!isLoggedIn(authentication)) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(roleName::equals);
    }
}
