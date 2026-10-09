package vn.talentbridge;

import org.junit.jupiter.api.Test;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.service.JobQualityScorer;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobQualityScorerTest {
    private final JobQualityScorer scorer = new JobQualityScorer();

    @Test
    void givesFullScoreOnlyWhenListingHasClearInformationAndApprovedCompanySignal() {
        Job job = new Job();
        job.setTitle("Backend Engineer");
        job.setDescription("x".repeat(220));
        job.setRequirements("x".repeat(120));
        job.setBenefits("x".repeat(60));
        job.setCity("Hà Nội");
        job.setJobType("FULL_TIME");
        job.setExperienceLevel("MID");
        job.setIsNegotiable(true);
        job.setDeadline(LocalDate.of(2027, 1, 1));
        job.setSkills(List.of("Java", "Spring", "SQL"));
        job.setStatus(JobStatus.ACTIVE);
        Company company = new Company();
        company.setStatus(CompanyStatus.APPROVED);
        company.setWebsite("https://example.com");

        var assessment = scorer.assess(job, company, LocalDate.of(2026, 10, 9));

        assertThat(assessment.qualityScore()).isEqualTo(100);
        assertThat(assessment.trustSignals()).contains("Doanh nghiệp có trạng thái APPROVED trong TalentBridge.");
        assertThat(assessment.limitation()).contains("không xác minh độc lập");
    }

    @Test
    void doesNotDescribeAnUnapprovedCompanyAsVerified() {
        Job job = new Job();
        job.setStatus(JobStatus.PENDING);

        var assessment = scorer.assess(job, null, LocalDate.of(2026, 10, 9));

        assertThat(assessment.qualityScore()).isLessThan(100);
        assertThat(assessment.trustSignals()).noneMatch(signal -> signal.contains("đã được duyệt"));
        assertThat(assessment.improvementSuggestions()).anyMatch(suggestion -> suggestion.contains("Hoàn thiện hồ sơ doanh nghiệp"));
    }

    @Test
    void doesNotGiveFullSalaryTransparencyForAnInvalidRangeEvenIfNegotiable() {
        Job job = new Job();
        job.setMinSalary(new java.math.BigDecimal("50000000"));
        job.setMaxSalary(new java.math.BigDecimal("30000000"));
        job.setIsNegotiable(true);

        var assessment = scorer.assess(job, null, LocalDate.of(2026, 10, 9));

        assertThat(assessment.criteria())
                .filteredOn(criterion -> criterion.name().equals("Thông tin lương"))
                .singleElement()
                .satisfies(criterion -> {
                    assertThat(criterion.points()).isZero();
                    assertThat(criterion.satisfied()).isFalse();
                    assertThat(criterion.evidence()).contains("không hợp lệ");
                });
    }
}
