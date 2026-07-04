package com.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(
        name = "app_users",
        uniqueConstraints = @UniqueConstraint(name = "uk_app_users_username", columnNames = "username")
)
public class AppUser {

    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AppUser() {
    }

    public AppUser(String username, String passwordHash, String role, Boolean enabled) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
    }

    @PrePersist
    public void applyCreateDefaults() {
        if (enabled == null) {
            enabled = true;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void applyUpdateDefaults() {
        if (enabled == null) {
            enabled = true;
        }
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAtDisplay() {
        return formatDateTime(createdAt, "Not recorded");
    }

    public String getUpdatedAtDisplay() {
        return formatDateTime(updatedAt, "Not updated");
    }

    public String getEnabledDisplay() {
        return Boolean.TRUE.equals(enabled) ? "Enabled" : "Disabled";
    }

    private String formatDateTime(LocalDateTime dateTime, String fallback) {
        if (dateTime == null) {
            return fallback;
        }
        return DISPLAY_DATE_TIME_FORMATTER.format(dateTime);
    }
}
