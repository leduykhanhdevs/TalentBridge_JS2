package vn.talentbridge.core.application.dto;

public record AdminDashboardStatsResult(
    long totalUsers,
    long totalCompanies,
    long pendingCompanies,
    long totalJobs,
    long activeJobs,
    long pendingJobs
) {}