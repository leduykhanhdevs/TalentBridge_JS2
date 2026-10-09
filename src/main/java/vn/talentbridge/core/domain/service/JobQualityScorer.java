package vn.talentbridge.core.domain.service;

import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.JobQualityAssessment;
import vn.talentbridge.core.domain.model.JobQualityCriterion;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JobQualityScorer {
    private static final String LIMITATION =
            "Điểm này đo mức độ đầy đủ và rõ ràng của dữ liệu tin cùng tín hiệu kiểm duyệt nội bộ; không xác minh độc lập doanh nghiệp, tính xác thực nội dung hoặc bảo đảm kết quả tuyển dụng.";

    public JobQualityAssessment assess(Job job, Company company, LocalDate today) {
        if (job == null) throw new IllegalArgumentException("Tin tuyển dụng không được để trống");
        if (today == null) throw new IllegalArgumentException("Ngày đánh giá không được để trống");

        List<JobQualityCriterion> criteria = new ArrayList<>();
        List<String> trustSignals = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        add(criteria, suggestions, "Tiêu đề", 10, nonBlank(job.getTitle()), 10,
                "Đã có tiêu đề cụ thể.", "Bổ sung tiêu đề vị trí rõ ràng.");
        addByLength(criteria, suggestions, "Mô tả công việc", 15, job.getDescription(), 200,
                "Mô tả nên nêu phạm vi công việc, trách nhiệm và đầu ra.");
        addByLength(criteria, suggestions, "Yêu cầu tuyển dụng", 15, job.getRequirements(), 100,
                "Nêu kỹ năng, kinh nghiệm hoặc tiêu chí bắt buộc.");
        addByLength(criteria, suggestions, "Quyền lợi", 8, job.getBenefits(), 40,
                "Bổ sung quyền lợi và chế độ đãi ngộ.");
        boolean hasLocation = nonBlank(job.getCity()) || nonBlank(job.getLocation());
        add(criteria, suggestions, "Địa điểm", 8, hasLocation, 8,
                "Đã nêu địa điểm làm việc.", "Nêu thành phố hoặc địa điểm làm việc.");
        boolean hasWorkDetails = nonBlank(job.getJobType()) && nonBlank(job.getExperienceLevel());
        add(criteria, suggestions, "Hình thức và kinh nghiệm", 8, hasWorkDetails, 8,
                "Đã nêu hình thức làm việc và cấp độ kinh nghiệm.", "Chọn hình thức làm việc và mức kinh nghiệm.");

        boolean hasSalaryRange = job.getMinSalary() != null || job.getMaxSalary() != null;
        boolean hasValidSalaryRange = job.getMinSalary() != null && job.getMaxSalary() != null
                && job.getMinSalary().compareTo(BigDecimal.ZERO) >= 0
                && job.getMinSalary().compareTo(job.getMaxSalary()) <= 0;
        boolean salaryTransparent = hasSalaryRange
                ? hasValidSalaryRange
                : Boolean.TRUE.equals(job.getIsNegotiable());
        boolean hasValidPartialRange = (job.getMinSalary() == null || job.getMinSalary().compareTo(BigDecimal.ZERO) >= 0)
                && (job.getMaxSalary() == null || job.getMaxSalary().compareTo(BigDecimal.ZERO) >= 0)
                && (job.getMinSalary() == null || job.getMaxSalary() == null
                        || job.getMinSalary().compareTo(job.getMaxSalary()) <= 0);
        int salaryPoints = salaryTransparent ? 10 : (hasSalaryRange && hasValidPartialRange ? 5 : 0);
        criteria.add(new JobQualityCriterion("Thông tin lương", salaryPoints, 10, salaryTransparent,
                salaryTransparent ? (hasValidSalaryRange ? "Có khoảng lương hợp lệ." : "Đã ghi rõ có thể thương lượng.")
                        : (hasSalaryRange ? "Khoảng lương thiếu cận hoặc có giá trị không hợp lệ."
                                : "Khoảng lương chưa được công khai.")));
        if (!salaryTransparent) suggestions.add("Công khai khoảng lương hoặc xác nhận có thể thương lượng.");
        if (salaryTransparent) trustSignals.add("Thông tin lương được khai báo rõ trên tin.");

        boolean deadlineValid = job.getDeadline() != null && job.getDeadline().isAfter(today);
        add(criteria, suggestions, "Hạn ứng tuyển", 8, deadlineValid, 8,
                "Hạn ứng tuyển còn ở tương lai.", "Đặt hạn ứng tuyển hợp lệ trong tương lai.");
        int skillPoints = job.getSkills() == null || job.getSkills().isEmpty()
                ? 0 : Math.min(10, job.getSkills().size() >= 3 ? 10 : 5);
        criteria.add(new JobQualityCriterion("Kỹ năng", skillPoints, 10, skillPoints == 10,
                skillPoints == 10 ? "Tin nêu ít nhất ba kỹ năng." : "Tin có ít kỹ năng hoặc chưa khai báo kỹ năng."));
        if (skillPoints < 10) suggestions.add("Thêm các kỹ năng quan trọng và thực sự cần cho vị trí.");

        boolean approved = company != null && company.getStatus() == CompanyStatus.APPROVED;
        boolean companyDetails = company != null && (nonBlank(company.getWebsite()) || nonBlank(company.getTaxCode()));
        int companyPoints = approved ? (companyDetails ? 8 : 4) : 0;
        criteria.add(new JobQualityCriterion("Thông tin doanh nghiệp", companyPoints, 8, approved && companyDetails,
                approved ? (companyDetails ? "Doanh nghiệp đã được duyệt trong TalentBridge và có thông tin liên hệ công khai."
                        : "Doanh nghiệp đã được duyệt trong TalentBridge; hồ sơ còn thiếu website hoặc mã số thuế.")
                        : "Chưa có tín hiệu doanh nghiệp đã được duyệt trong TalentBridge."));
        if (approved) trustSignals.add("Doanh nghiệp có trạng thái APPROVED trong TalentBridge.");
        else suggestions.add("Hoàn thiện hồ sơ doanh nghiệp và gửi Admin xét duyệt.");

        boolean active = job.getStatus() == JobStatus.ACTIVE;
        if (active) trustSignals.add("Tin có trạng thái ACTIVE trong TalentBridge.");
        if (deadlineValid) trustSignals.add("Hạn ứng tuyển chưa qua.");
        if (!active) suggestions.add("Kiểm tra trạng thái kiểm duyệt nếu tin cần hiển thị công khai.");

        int score = criteria.stream().mapToInt(JobQualityCriterion::points).sum();
        String label = score >= 85 ? "Chỉnh chu" : score >= 65 ? "Cần bổ sung" : "Thiếu thông tin";
        return new JobQualityAssessment(score, label, criteria, trustSignals, suggestions, LIMITATION);
    }

    private void addByLength(List<JobQualityCriterion> criteria, List<String> suggestions,
                             String name, int maximum, String value, int completeLength, String suggestion) {
        int length = value == null ? 0 : value.trim().length();
        int points = length >= completeLength ? maximum : length > 0 ? maximum / 2 : 0;
        criteria.add(new JobQualityCriterion(name, points, maximum, points == maximum,
                points == maximum ? "Nội dung đạt độ dài tối thiểu đề xuất." : "Nội dung còn thiếu hoặc ngắn."));
        if (points < maximum) suggestions.add(suggestion);
    }

    private void add(List<JobQualityCriterion> criteria, List<String> suggestions,
                     String name, int maximum, boolean satisfied, int points,
                     String evidence, String suggestion) {
        criteria.add(new JobQualityCriterion(name, satisfied ? points : 0, maximum, satisfied, evidence));
        if (!satisfied) suggestions.add(suggestion);
    }

    private boolean nonBlank(String value) {
        return value != null && !value.isBlank();
    }
}
