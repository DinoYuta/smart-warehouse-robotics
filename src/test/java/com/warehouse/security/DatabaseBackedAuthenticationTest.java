package com.warehouse.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.warehouse.model.AppUser;
import com.warehouse.repository.AppUserRepository;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DatabaseBackedAuthenticationTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void seededDemoUsersAreLoadedFromDatabaseWithBcryptHashes() {
        assertDatabaseUser("Admin", "admin", "ADMIN");
        assertDatabaseUser("Manage", "manage", "MANAGE");
        assertDatabaseUser("Nova001", "nova001", "STAFF");

        assertLoadedAuthorities("admin", "ROLE_ADMIN", "ROLE_MANAGE", "ROLE_STAFF");
        assertLoadedAuthorities("manage", "ROLE_MANAGE", "ROLE_STAFF");
        assertLoadedAuthorities("nova001", "ROLE_STAFF");
    }

    @Test
    void bcryptLoginWorksForStaffAndInvalidPasswordFails() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "Nova001")
                        .param("password", "nova001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/pickup-request"));

        mockMvc.perform(post("/login")
                        .param("username", "Nova001")
                        .param("password", "wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void disabledUserCannotLogin() throws Exception {
        AppUser disabledUser = appUserRepository.findByUsernameIgnoreCase("DisabledStaff")
                .orElseGet(AppUser::new);
        disabledUser.setUsername("DisabledStaff");
        disabledUser.setPasswordHash(passwordEncoder.encode("disabled"));
        disabledUser.setRole("STAFF");
        disabledUser.setEnabled(false);
        appUserRepository.save(disabledUser);

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("DisabledStaff"))
                .isInstanceOf(UsernameNotFoundException.class);

        mockMvc.perform(post("/login")
                        .param("username", "DisabledStaff")
                        .param("password", "disabled"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void loginPageShowsOnlyStaffDemoCredentialsAndGenericError() throws Exception {
        mockMvc.perform(get("/login").param("error", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Invalid username or password")))
                .andExpect(content().string(containsString("Public Staff mobile demo")))
                .andExpect(content().string(containsString("Nova001 / nova001")))
                .andExpect(content().string(not(containsString("Admin / admin"))))
                .andExpect(content().string(not(containsString("Manage / manage"))));
    }

    private void assertDatabaseUser(String username, String rawPassword, String role) {
        AppUser appUser = appUserRepository.findByUsernameIgnoreCase(username.toLowerCase())
                .orElseThrow();

        assertThat(appUser.getUsername()).isEqualTo(username);
        assertThat(appUser.getRole()).isEqualTo(role);
        assertThat(appUser.getEnabled()).isTrue();
        assertThat(appUser.getCreatedAt()).isNotNull();
        assertThat(appUser.getPasswordHash()).isNotEqualTo(rawPassword);
        assertThat(appUser.getPasswordHash()).matches("^\\$2[aby]\\$.+");
        assertThat(passwordEncoder.matches(rawPassword, appUser.getPasswordHash())).isTrue();
    }

    private void assertLoadedAuthorities(String username, String... expectedAuthorities) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Set<String> authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorities).containsExactlyInAnyOrder(expectedAuthorities);
    }
}
