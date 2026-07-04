package com.warehouse.controller;

import com.warehouse.dto.ChangePasswordFormDto;
import com.warehouse.model.AppUser;
import com.warehouse.service.AdminUserService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminUserManagementController {

    private final AdminUserService adminUserService;

    public AdminUserManagementController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/admin/users")
    public String showUsers(Model model) {
        List<AppUser> users = adminUserService.getUsers();
        model.addAttribute("users", users);
        return "admin-users";
    }

    @GetMapping("/admin/users/{id}/change-password")
    public String showChangePassword(@PathVariable("id") Long userId,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("user", adminUserService.getUser(userId));
            if (!model.containsAttribute("changePasswordForm")) {
                model.addAttribute("changePasswordForm", new ChangePasswordFormDto());
            }
            return "admin-change-password";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/admin/users/{id}/change-password")
    public String changePassword(@PathVariable("id") Long userId,
                                 @ModelAttribute("changePasswordForm") ChangePasswordFormDto form,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            adminUserService.changePassword(userId, form);
            redirectAttributes.addFlashAttribute("successMessage", AdminUserService.PASSWORD_UPDATED_MESSAGE);
            return "redirect:/admin/users";
        } catch (IllegalArgumentException ex) {
            List<String> validationErrors = adminUserService.validatePasswordForm(form);
            if (validationErrors.isEmpty()) {
                validationErrors = List.of(ex.getMessage());
            }
            model.addAttribute("user", safeUser(userId));
            model.addAttribute("validationErrors", validationErrors);
            return "admin-change-password";
        }
    }

    private AppUser safeUser(Long userId) {
        try {
            return adminUserService.getUser(userId);
        } catch (IllegalArgumentException ex) {
            AppUser missingUser = new AppUser();
            missingUser.setId(userId);
            missingUser.setUsername("Unknown user");
            missingUser.setRole("UNKNOWN");
            missingUser.setEnabled(false);
            return missingUser;
        }
    }
}
