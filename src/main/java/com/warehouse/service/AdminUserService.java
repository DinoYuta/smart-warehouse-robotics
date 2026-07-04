package com.warehouse.service;

import com.warehouse.dto.ChangePasswordFormDto;
import com.warehouse.model.AppUser;
import com.warehouse.repository.AppUserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminUserService {

    public static final String PASSWORD_REQUIRED_MESSAGE = "Password is required.";
    public static final String PASSWORD_MIN_LENGTH_MESSAGE = "Password must be at least 8 characters.";
    public static final String PASSWORD_MAX_LENGTH_MESSAGE = "Password must be 100 characters or fewer.";
    public static final String PASSWORD_CONFIRMATION_MISMATCH_MESSAGE =
            "Password confirmation does not match.";
    public static final String PASSWORD_UPDATED_MESSAGE = "Password updated successfully.";

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 100;

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(AppUserRepository appUserRepository,
                            PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AppUser> getUsers() {
        return appUserRepository.findAllByOrderByUsernameAsc();
    }

    @Transactional(readOnly = true)
    public AppUser getUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    public AppUser changePassword(Long userId, ChangePasswordFormDto form) {
        List<String> validationErrors = validatePasswordForm(form);
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", validationErrors));
        }

        AppUser appUser = getUser(userId);
        appUser.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        return appUserRepository.save(appUser);
    }

    public List<String> validatePasswordForm(ChangePasswordFormDto form) {
        List<String> errors = new ArrayList<>();
        String newPassword = form.getNewPassword();
        String confirmPassword = form.getConfirmPassword();

        if (isBlank(newPassword)) {
            errors.add(PASSWORD_REQUIRED_MESSAGE);
            return errors;
        }
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            errors.add(PASSWORD_MIN_LENGTH_MESSAGE);
        }
        if (newPassword.length() > MAX_PASSWORD_LENGTH) {
            errors.add(PASSWORD_MAX_LENGTH_MESSAGE);
        }
        if (confirmPassword == null || !newPassword.contentEquals(confirmPassword)) {
            errors.add(PASSWORD_CONFIRMATION_MISMATCH_MESSAGE);
        }

        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
