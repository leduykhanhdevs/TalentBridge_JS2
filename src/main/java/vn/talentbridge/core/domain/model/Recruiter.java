package vn.talentbridge.core.domain.model;

import java.time.LocalDateTime;

public class Recruiter {
    private Long id;
    private User user;
    private Company company;
    private String position;
    private LocalDateTime createdAt;

    public Recruiter() {
    }

    public Recruiter(Long id, User user, Company company, String position, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.company = company;
        this.position = position;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}