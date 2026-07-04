package com.warehouse.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            UserDetailsService userDetailsService,
                                            PasswordEncoder passwordEncoder)
            throws Exception {
        http
                // Existing Thymeleaf forms do not include CSRF tokens.
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(databaseAuthenticationProvider(userDetailsService, passwordEncoder))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/access-denied",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/admin/security-demos/sql-injection/**").hasRole("ADMIN")
                        .requestMatchers("/admin/security-demos/bcrypt-password/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/settings").hasAnyRole("STAFF", "MANAGE", "ADMIN")
                        .requestMatchers("/staff/**").hasAnyRole("STAFF", "MANAGE", "ADMIN")
                        .requestMatchers("/manager/**").hasAnyRole("MANAGE", "ADMIN")
                        .requestMatchers(
                                "/",
                                "/dashboard",
                                "/rules/**",
                                "/robots/**",
                                "/simulation/**",
                                "/system-flow/**"
                        )
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(roleRedirectSuccessHandler())
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception.accessDeniedPage("/access-denied"));

        return http.build();
    }

    private AuthenticationProvider databaseAuthenticationProvider(UserDetailsService userDetailsService,
                                                                  PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationSuccessHandler roleRedirectSuccessHandler() {
        return new RoleRedirectSuccessHandler();
    }

    private static class RoleRedirectSuccessHandler implements AuthenticationSuccessHandler {

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Authentication authentication)
                throws IOException, ServletException {
            response.sendRedirect(resolveLandingPage(authentication));
        }

        private String resolveLandingPage(Authentication authentication) {
            if (hasRole(authentication, "ROLE_ADMIN")) {
                return "/dashboard";
            }
            if (hasRole(authentication, "ROLE_MANAGE")) {
                return "/manager/robot-tasks";
            }
            return "/staff/pickup-request";
        }

        private boolean hasRole(Authentication authentication, String roleName) {
            if (authentication == null) {
                return false;
            }
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(roleName::equals);
        }
    }
}
