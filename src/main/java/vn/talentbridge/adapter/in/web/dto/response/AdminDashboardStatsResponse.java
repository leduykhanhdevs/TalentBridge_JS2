package vn.talentbridge.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.talentbridge.core.application.dto.AdminDashboardStatsResult;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardStatsResponse {

    private long totalUsers;
    private long totalCompanies;
    private long pendingCompanies;
    private long totalJobs;
    private long activeJobs;
    private long pendingJobs;

    public static AdminDashboardStatsResponse from(AdminDashboardStatsResult result) {
        return AdminDashboardStatsResponse.builder()
                .totalUsers(result.totalUsers())
                .totalCompanies(result.totalCompanies())
                .pendingCompanies(result.pendingCompanies())
                .totalJobs(result.totalJobs())
                .activeJobs(result.activeJobs())
                .pendingJobs(result.pendingJobs())
                .build();
    }
}