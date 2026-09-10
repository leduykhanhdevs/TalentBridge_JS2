package vn.talentbridge.modules.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}