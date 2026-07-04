package com.warehouse.service;

import com.warehouse.model.AppUser;
import com.warehouse.repository.AppUserRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public CustomUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));

        if (!Boolean.TRUE.equals(appUser.getEnabled())) {
            throw new UsernameNotFoundException("Invalid username or password");
        }

        return User.withUsername(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .authorities(resolveAuthorities(appUser.getRole()))
                .build();
    }

    private List<GrantedAuthority> resolveAuthorities(String role) {
        String normalizedRole = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        return switch (normalizedRole) {
            case "ADMIN" -> List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_MANAGE"),
                    new SimpleGrantedAuthority("ROLE_STAFF")
            );
            case "MANAGE" -> List.of(
                    new SimpleGrantedAuthority("ROLE_MANAGE"),
                    new SimpleGrantedAuthority("ROLE_STAFF")
            );
            case "STAFF" -> List.of(new SimpleGrantedAuthority("ROLE_STAFF"));
            default -> throw new UsernameNotFoundException("Invalid username or password");
        };
    }
}
