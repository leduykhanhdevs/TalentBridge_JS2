package vn.talentbridge.core.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Candidate {
    private Long id;
    private User user;
    private String title;
    private LocalDate dob;
    private String gender;
    private String summary;
    private Integer experienceYears;
    private BigDecimal currentSalary;
    private BigDecimal expectedSalary;
    private String city;
    private String address;
    private String personalWebsite;
    private String linkedinUrl;
    private String githubUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Candidate() {
    }

    public Candidate(Long id, User user, String title, LocalDate dob, String gender, String summary,
                     Integer experienceYears, BigDecimal currentSalary, BigDecimal expectedSalary,
                     String city, String address, String personalWebsite, String linkedinUrl,
                     String githubUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.dob = dob;
        this.gender = gender;
        this.summary = summary;
        this.experienceYears = experienceYears;
        this.currentSalary = currentSalary;
        this.expectedSalary = expectedSalary;
        this.city = city;
        this.address = address;
        this.personalWebsite = personalWebsite;
        this.linkedinUrl = linkedinUrl;
        this.githubUrl = githubUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public BigDecimal getCurrentSalary() {
        return currentSalary;
    }

    public void setCurrentSalary(BigDecimal currentSalary) {
        this.currentSalary = currentSalary;
    }

    public BigDecimal getExpectedSalary() {
        return expectedSalary;
    }

    public void setExpectedSalary(BigDecimal expectedSalary) {
        this.expectedSalary = expectedSalary;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPersonalWebsite() {
        return personalWebsite;
    }

    public void setPersonalWebsite(String personalWebsite) {
        this.personalWebsite = personalWebsite;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
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
