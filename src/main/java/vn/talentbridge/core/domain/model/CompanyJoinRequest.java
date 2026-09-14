package vn.talentbridge.core.domain.model;

import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;

import java.time.LocalDateTime;

public class CompanyJoinRequest {
    private Long id;
    private User user;
    private Company company;
    private String position;
    private String message;
    private CompanyJoinRequestStatus status;
    private String reason;
    private Long approvedByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CompanyJoinRequest() {
        this.status = CompanyJoinRequestStatus.PENDING;
    }

    public CompanyJoinRequest(Long id, User user, Company company, String position, String message,
                              CompanyJoinRequestStatus status, String reason, Long approvedByUserId,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.company = company;
        this.position = position;
        this.message = message;
        this.status = status != null ? status : CompanyJoinRequestStatus.PENDING;
        this.reason = reason;
        this.approvedByUserId = approvedByUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void accept(Long approvedByUserId, String reason) {
        this.status = CompanyJoinRequestStatus.ACCEPTED;
        this.approvedByUserId = approvedByUserId;
        this.reason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(Long approvedByUserId, String reason) {
        this.status = CompanyJoinRequestStatus.REJECTED;
        this.approvedByUserId = approvedByUserId;
        this.reason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CompanyJoinRequestStatus getStatus() {
        return status;
    }

    public void setStatus(CompanyJoinRequestStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getApprovedByUserId() {
        return approvedByUserId;
    }

    public void setApprovedByUserId(Long approvedByUserId) {
        this.approvedByUserId = approvedByUserId;
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
}
