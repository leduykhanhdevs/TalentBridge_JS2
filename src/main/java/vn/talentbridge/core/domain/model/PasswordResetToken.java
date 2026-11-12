package vn.talentbridge.core.domain.model;

import java.time.LocalDateTime;

public class PasswordResetToken {

    public enum Purpose {
        OTP,
        RESET_GRANT,
        LEGACY,
        EMAIL_VERIFICATION,
        PHONE_VERIFICATION
    }

    private Long id;
    private Long userId;
    private String tokenHash;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private Purpose purpose;
    private int failedAttempts;

    public PasswordResetToken() {
    }

    public PasswordResetToken(
            Long id,
            Long userId,
            String tokenHash,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            LocalDateTime usedAt
    ) {
        this(id, userId, tokenHash, createdAt, expiresAt, usedAt, Purpose.RESET_GRANT, 0);
    }

    public PasswordResetToken(
            Long id,
            Long userId,
            String tokenHash,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            LocalDateTime usedAt,
            Purpose purpose,
            int failedAttempts
    ) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.purpose = purpose;
        this.failedAttempts = failedAttempts;
    }

    public boolean isUsableAt(LocalDateTime time) {
        return usedAt == null
                && expiresAt != null
                && time.isBefore(expiresAt);
    }

    public void markUsed(LocalDateTime time) {
        if (usedAt == null) {
            usedAt = time;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public Purpose getPurpose() {
        return purpose;
    }

    public void setPurpose(Purpose purpose) {
        this.purpose = purpose;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
}
