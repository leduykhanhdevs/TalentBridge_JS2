package vn.talentbridge.core.domain.model;

import java.time.LocalDateTime;

public class AuthSession {

    private Long id;
    private String sessionId;
    private Long userId;
    private String refreshTokenHash;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;

    public AuthSession() {
    }

    public AuthSession(
            Long id,
            String sessionId,
            Long userId,
            String refreshTokenHash,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            LocalDateTime revokedAt
    ) {
        this.id = id;
        this.sessionId = sessionId;
        this.userId = userId;
        this.refreshTokenHash = refreshTokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public boolean isActiveAt(LocalDateTime time) {
        return revokedAt == null
                && expiresAt != null
                && time.isBefore(expiresAt);
    }

    public void revoke(LocalDateTime time) {
        if (this.revokedAt == null) {
            this.revokedAt = time;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public void setRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
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

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}