package vn.talentbridge.core.domain.model;

import vn.talentbridge.core.domain.vo.RoleName;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class User {
    private Long id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private UserStatus status;
    private Set<Role> roles = new HashSet<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {
        this.status = UserStatus.ACTIVE;
    }

    public User(Long id, String email, String passwordHash, String fullName, String phoneNumber,
                String avatarUrl, UserStatus status, Set<Role> roles,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.status = status != null ? status : UserStatus.ACTIVE;
        if (roles != null) {
            this.roles = roles;
        }
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean hasRole(RoleName roleName) {
        return roles.stream().anyMatch(r -> r.getName() == roleName);
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void lock() {
        this.status = UserStatus.LOCKED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}