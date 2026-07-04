package com.warehouse.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.warehouse.model.AppUser;
import com.warehouse.repository.AppUserRepository;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminUserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void adminCanOpenUserManagementButManageAndStaffCannot() throws Exception {
        MockHttpSession adminSession = login("Admin", "admin", "/dashboard");
        mockMvc.perform(get("/admin/users").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(content().string(containsString("User Management")))
                .andExpect(content().string(containsString("Admin")))
                .andExpect(content().string(containsString("Manage")))
                .andExpect(content().string(containsString("Nova001")))
                .andExpect(content().string(containsString("Change Password")))
                .andExpect(content().string(not(containsString("password_hash"))))
                .andExpect(content().string(not(containsString("passwordHash"))))
                .andExpect(content().string(not(containsString(user("Nova001").getPasswordHash()))));

        MockHttpSession manageSession = login("Manage", "manage", "/manager/robot-tasks");
        mockMvc.perform(get("/admin/users").session(manageSession))
                .andExpect(status().isForbidden());

        MockHttpSession staffSession = login("Nova001", "nova001", "/staff/pickup-request");
        mockMvc.perform(get("/admin/users").session(staffSession))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePasswordPageLoadsForAdminWithoutShowingHash() throws Exception {
        MockHttpSession adminSession = login("Admin", "admin", "/dashboard");
        AppUser nova = user("Nova001");

        mockMvc.perform(get("/admin/users/{id}/change-password", nova.getId()).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-change-password"))
                .andExpect(model().attributeExists("user", "changePasswordForm"))
                .andExpect(content().string(containsString("Change Password")))
                .andExpect(content().string(containsString("New Password")))
                .andExpect(content().string(containsString("Confirm Password")))
                .andExpect(content().string(containsString("Save")))
                .andExpect(content().string(containsString("Cancel")))
                .andExpect(content().string(containsString("Nova001")))
                .andExpect(content().string(not(containsString(nova.getPasswordHash()))))
                .andExpect(content().string(not(containsString("password_hash"))));
    }

    @Test
    void passwordValidationRejectsMissingShortAndMismatchedValues() throws Exception {
        MockHttpSession adminSession = login("Admin", "admin", "/dashboard");
        Long novaId = user("Nova001").getId();

        mockMvc.perform(post("/admin/users/{id}/change-password", novaId)
                        .session(adminSession)
                        .param("newPassword", "")
                        .param("confirmPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-change-password"))
                .andExpect(model().attributeExists("validationErrors"))
                .andExpect(content().string(containsString("Password is required.")));

        mockMvc.perform(post("/admin/users/{id}/change-password", novaId)
                        .session(adminSession)
                        .param("newPassword", "short")
                        .param("confirmPassword", "short"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Password must be at least 8 characters.")));

        mockMvc.perform(post("/admin/users/{id}/change-password", novaId)
                        .session(adminSession)
                        .param("newPassword", "nova12345")
                        .param("confirmPassword", "different123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Password confirmation does not match.")));
    }

    @Test
    void adminCanChangeNovaPasswordAndOldPasswordNoLongerWorks() throws Exception {
        MockHttpSession adminSession = login("Admin", "admin", "/dashboard");
        AppUser before = user("Nova001");
        String oldHash = before.getPasswordHash();

        mockMvc.perform(post("/admin/users/{id}/change-password", before.getId())
                        .session(adminSession)
                        .param("newPassword", "nova12345")
                        .param("confirmPassword", "nova12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attribute("successMessage", "Password updated successfully."));

        AppUser after = user("Nova001");
        assertThat(after.getPasswordHash()).isNotEqualTo(oldHash);
        assertThat(after.getPasswordHash()).matches("^\\$2[aby]\\$.+");
        assertThat(after.getPasswordHash()).isNotEqualTo("nova12345");
        assertThat(passwordEncoder.matches("nova12345", after.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("nova001", after.getPasswordHash())).isFalse();

        loginFails("Nova001", "nova001");
        login("Nova001", "nova12345", "/staff/pickup-request");
    }

    @Test
    void adminCanChangeManagePassword() throws Exception {
        MockHttpSession adminSession = login("Admin", "admin", "/dashboard");
        AppUser manage = user("Manage");
        String oldHash = manage.getPasswordHash();

        mockMvc.perform(post("/admin/users/{id}/change-password", manage.getId())
                        .session(adminSession)
                        .param("newPassword", "manage12345")
                        .param("confirmPassword", "manage12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        AppUser changedManage = user("Manage");
        assertThat(changedManage.getPasswordHash()).isNotEqualTo(oldHash);
        assertThat(changedManage.getPasswordHash()).matches("^\\$2[aby]\\$.+");
        loginFails("Manage", "manage");
        login("Manage", "manage12345", "/manager/robot-tasks");
    }

    @Test
    void adminCanChangeAdminPassword() throws Exception {
        MockHttpSession adminSession = login("Admin", "admin", "/dashboard");
        AppUser admin = user("Admin");
        String oldHash = admin.getPasswordHash();

        mockMvc.perform(post("/admin/users/{id}/change-password", admin.getId())
                        .session(adminSession)
                        .param("newPassword", "admin12345")
                        .param("confirmPassword", "admin12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        AppUser changedAdmin = user("Admin");
        assertThat(changedAdmin.getPasswordHash()).isNotEqualTo(oldHash);
        assertThat(changedAdmin.getPasswordHash()).matches("^\\$2[aby]\\$.+");
        loginFails("Admin", "admin");
        login("Admin", "admin12345", "/dashboard");
    }

    @Test
    void existingAuthenticationRoleProtectionStaffWorkflowAndLiveMapStillWork() throws Exception {
        MockHttpSession staffSession = login("Nova001", "nova001", "/staff/pickup-request");

        mockMvc.perform(get("/staff/pickup-request").session(staffSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Pickup Request")));
        mockMvc.perform(get("/staff/live-map/state").session(staffSession))
                .andExpect(status().isOk());
        mockMvc.perform(get("/rules").session(staffSession))
                .andExpect(status().isForbidden());
    }

    @Test
    void vietnameseUserManagementTranslationKeysExist() throws Exception {
        String settingsScript = Files.readString(
                Path.of("src/main/resources/static/js/app-settings.js"),
                StandardCharsets.UTF_8
        );

        assertThat(settingsScript)
                .contains("Quản lý tài khoản")
                .contains("Đổi mật khẩu")
                .contains("Mật khẩu mới")
                .contains("Xác nhận mật khẩu")
                .contains("Lưu")
                .contains("Hủy")
                .contains("Cập nhật mật khẩu thành công.")
                .contains("Vui lòng nhập mật khẩu.")
                .contains("Mật khẩu phải có ít nhất 8 ký tự.")
                .contains("Xác nhận mật khẩu không khớp.");
    }

    private AppUser user(String username) {
        return appUserRepository.findByUsernameIgnoreCase(username).orElseThrow();
    }

    private MockHttpSession login(String username, String password, String expectedRedirect) throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirect))
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private void loginFails(String username, String password) throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }
}
