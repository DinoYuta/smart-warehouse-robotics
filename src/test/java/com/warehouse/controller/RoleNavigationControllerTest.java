package com.warehouse.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class RoleNavigationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginPageShowsOnlyStaffDemoUser() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Public Staff mobile demo")))
                .andExpect(content().string(not(containsString("Admin / admin"))))
                .andExpect(content().string(not(containsString("Manage / manage"))))
                .andExpect(content().string(containsString("Nova001 / nova001")));
    }

    @Test
    void successfulLoginRedirectsByRole() throws Exception {
        login("Admin", "admin", "/dashboard");
        login("Manage", "manage", "/manager/robot-tasks");
        login("Nova001", "nova001", "/staff/pickup-request");
    }

    @Test
    void adminNavigationShowsAdminManagerAndStaffLinks() throws Exception {
        MockHttpSession session = login("Admin", "admin", "/dashboard");

        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Dashboard")))
                .andExpect(content().string(containsString("Rule Management")))
                .andExpect(content().string(containsString("Robot Management")))
                .andExpect(content().string(containsString("Simulation")))
                .andExpect(content().string(containsString("System Flow")))
                .andExpect(content().string(containsString("User Management")))
                .andExpect(content().string(containsString("Robot Task Board")))
                .andExpect(content().string(containsString("Rule / Policy Assignment")))
                .andExpect(content().string(containsString("Customer Pickup Codes")))
                .andExpect(content().string(containsString("Create Pickup Request")))
                .andExpect(content().string(containsString("My Missions")))
                .andExpect(content().string(containsString("Live Warehouse Map")))
                .andExpect(content().string(containsString("Settings")))
                .andExpect(content().string(containsString("data-settings-widget")))
                .andExpect(content().string(containsString("topbar-language-setting")))
                .andExpect(content().string(containsString("data-user-widget")))
                .andExpect(content().string(containsString("data-notification-widget")))
                .andExpect(content().string(containsString("Logout")))
                .andExpect(content().string(not(containsString("href=\"/settings\""))));
    }

    @Test
    void managerNavigationShowsManagerAndStaffLinksOnly() throws Exception {
        MockHttpSession session = login("Manage", "manage", "/manager/robot-tasks");

        mockMvc.perform(get("/manager/robot-tasks").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Robot Task Board")))
                .andExpect(content().string(containsString("Rule / Policy Assignment")))
                .andExpect(content().string(containsString("Customer Pickup Codes")))
                .andExpect(content().string(containsString("Create Pickup Request")))
                .andExpect(content().string(containsString("My Missions")))
                .andExpect(content().string(containsString("Live Warehouse Map")))
                .andExpect(content().string(containsString("Settings")))
                .andExpect(content().string(containsString("data-settings-widget")))
                .andExpect(content().string(containsString("topbar-language-setting")))
                .andExpect(content().string(containsString("data-user-widget")))
                .andExpect(content().string(containsString("data-notification-widget")))
                .andExpect(content().string(containsString("Logout")))
                .andExpect(content().string(not(containsString("href=\"/settings\""))))
                .andExpect(content().string(not(containsString("/dashboard"))))
                .andExpect(content().string(not(containsString("/admin/users"))))
                .andExpect(content().string(not(containsString("/rules"))))
                .andExpect(content().string(not(containsString("/robots"))))
                .andExpect(content().string(not(containsString("/simulation"))))
                .andExpect(content().string(not(containsString("/system-flow"))))
                .andExpect(content().string(not(containsString("Mission Monitor"))))
                .andExpect(content().string(not(containsString("Robot Operations"))));
    }

    @Test
    void staffNavigationShowsStaffLinksOnly() throws Exception {
        MockHttpSession session = login("Nova001", "nova001", "/staff/pickup-request");

        mockMvc.perform(get("/staff/missions").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Pickup Request")))
                .andExpect(content().string(containsString("My Missions")))
                .andExpect(content().string(containsString("Live Warehouse Map")))
                .andExpect(content().string(containsString("Settings")))
                .andExpect(content().string(containsString("data-settings-widget")))
                .andExpect(content().string(containsString("topbar-language-setting")))
                .andExpect(content().string(containsString("data-user-widget")))
                .andExpect(content().string(containsString("data-notification-widget")))
                .andExpect(content().string(containsString("Logout")))
                .andExpect(content().string(not(containsString("href=\"/settings\""))))
                .andExpect(content().string(not(containsString("/manager/robot-tasks"))))
                .andExpect(content().string(not(containsString("/manager/policy-assignment"))))
                .andExpect(content().string(not(containsString("/manager/customer-pickup-codes"))))
                .andExpect(content().string(not(containsString("/dashboard"))))
                .andExpect(content().string(not(containsString("/admin/users"))))
                .andExpect(content().string(not(containsString("/rules"))))
                .andExpect(content().string(not(containsString("/robots"))))
                .andExpect(content().string(not(containsString("/simulation"))))
                .andExpect(content().string(not(containsString("/system-flow"))))
                .andExpect(content().string(not(containsString("Mission Monitor"))))
                .andExpect(content().string(not(containsString("Robot Operations"))));
    }

    @Test
    void staffSidebarLiveMapLinkOpensInNewTab() throws Exception {
        MockHttpSession session = login("Nova001", "nova001", "/staff/pickup-request");

        mockMvc.perform(get("/staff/missions").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Live Warehouse Map")))
                .andExpect(content().string(containsString("/staff/live-map")))
                .andExpect(content().string(containsString("target=\"_blank\"")))
                .andExpect(content().string(containsString("rel=\"noopener noreferrer\"")));
    }

    @Test
    void roleAccessRulesProtectRoutes() throws Exception {
        mockMvc.perform(get("/staff/pickup-request"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/settings"))
                .andExpect(status().is3xxRedirection());

        MockHttpSession staffSession = login("Nova001", "nova001", "/staff/pickup-request");
        mockMvc.perform(get("/staff/pickup-request").session(staffSession))
                .andExpect(status().isOk());
        mockMvc.perform(get("/settings").session(staffSession))
                .andExpect(status().isOk());
        mockMvc.perform(get("/staff/live-map/state").session(staffSession))
                .andExpect(status().isOk());
        mockMvc.perform(get("/manager/robot-tasks").session(staffSession))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/users").session(staffSession))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/manager/customer-pickup-codes").session(staffSession))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/rules").session(staffSession))
                .andExpect(status().isForbidden());

        MockHttpSession manageSession = login("Manage", "manage", "/manager/robot-tasks");
        mockMvc.perform(get("/manager/policy-assignment").session(manageSession))
                .andExpect(status().isOk());
        mockMvc.perform(get("/manager/customer-pickup-codes").session(manageSession))
                .andExpect(status().isOk());
        mockMvc.perform(get("/staff/missions").session(manageSession))
                .andExpect(status().isOk());
        mockMvc.perform(get("/settings").session(manageSession))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/users").session(manageSession))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/robots").session(manageSession))
                .andExpect(status().isForbidden());

        MockHttpSession adminSession = login("Admin", "admin", "/dashboard");
        String[] adminRoutes = {
                "/dashboard",
                "/admin/users",
                "/rules",
                "/robots",
                "/simulation",
                "/system-flow",
                "/manager/policy-assignment",
                "/manager/robot-tasks",
                "/manager/customer-pickup-codes",
                "/staff/pickup-request",
                "/staff/missions",
                "/staff/live-map",
                "/settings"
        };

        for (String route : adminRoutes) {
            mockMvc.perform(get(route).session(adminSession))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void settingsPageShowsUserPreferencesForAllRoles() throws Exception {
        MockHttpSession adminSession = login("Admin", "admin", "/dashboard");
        MockHttpSession manageSession = login("Manage", "manage", "/manager/robot-tasks");
        MockHttpSession staffSession = login("Nova001", "nova001", "/staff/pickup-request");

        assertSettingsPage(adminSession);
        assertSettingsPage(manageSession);
        assertSettingsPage(staffSession);
    }

    @Test
    void settingsScriptContainsNaturalVietnameseLabelsAndMessages() throws Exception {
        String settingsScript = Files.readString(
                Path.of("src/main/resources/static/js/app-settings.js"),
                StandardCharsets.UTF_8
        );

        assertThat(settingsScript)
                .contains("Cài đặt")
                .contains("Thông báo")
                .contains("Đăng xuất")
                .contains("Robot đang thực hiện nhiệm vụ.")
                .contains("Robot đã về trạm. Chờ xác nhận hoàn tất.")
                .contains("Chỉ có thể hoàn tất nhiệm vụ sau khi robot đã về trạm.")
                .contains("Di chuyển nhanh")
                .contains("Tiết kiệm năng lượng")
                .contains("Tránh vật cản");
    }

    @Test
    void logoutRedirectsToLoginPage() throws Exception {
        MockHttpSession session = login("Nova001", "nova001", "/staff/pickup-request");

        mockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
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

    private void assertSettingsPage(MockHttpSession session) throws Exception {
        mockMvc.perform(get("/settings").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Language")))
                .andExpect(content().string(containsString("English")))
                .andExpect(content().string(containsString("Tiếng Việt")))
                .andExpect(content().string(containsString("Theme")))
                .andExpect(content().string(containsString("Light mode")))
                .andExpect(content().string(containsString("Dark mode")))
                .andExpect(content().string(containsString("UI Density")))
                .andExpect(content().string(containsString("Compact")))
                .andExpect(content().string(containsString("Comfortable")))
                .andExpect(content().string(containsString("Settings are saved for this browser session/device.")))
                .andExpect(content().string(containsString("data-notification-widget")))
                .andExpect(content().string(containsString("/js/app-settings.js")))
                .andExpect(content().string(containsString("/js/app-notifications.js")));
    }
}
