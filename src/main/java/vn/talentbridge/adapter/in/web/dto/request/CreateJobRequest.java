package vn.talentbridge.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateJobRequest {

    @NotBlank(message = "Tiêu đề công việc không được để trống")
    @Size(max = 200, message = "Tiêu đề công việc không được vượt quá 200 ký tự")
    @Schema(example = "Senior Java Spring Boot Developer", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "Mô tả công việc không được để trống")
    @Schema(example = "Phát triển và bảo trì hệ thống tuyển dụng Microservices...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(example = "- Tối thiểu 3 năm kinh nghiệm với Java/Spring Boot\n- Thành thạo MySQL, Redis, Docker")
    private String requirements;

    @Schema(example = "- Lương thưởng tháng 13, KPI performance bonus\n- Bảo hiểm sức khỏe quốc tế PTI")
    private String benefits;

    @Schema(example = "Tại văn phòng (Hybrid)")
    private String location;

    @NotBlank(message = "Thành phố không được để trống")
    @Schema(example = "Hồ Chí Minh", requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;

    @Schema(example = "Khu Công Nghệ Cao, TP. Thủ Đức")
    private String address;

    @NotBlank(message = "Hình thức làm việc không được để trống")
    @Schema(example = "FULL_TIME", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jobType;

    @NotBlank(message = "Cấp bậc/Kinh nghiệm không được để trống")
    @Schema(example = "SENIOR", requiredMode = Schema.RequiredMode.REQUIRED)
    private String experienceLevel;

    @PositiveOrZero(message = "Mức lương tối thiểu không được âm")
    @Schema(example = "25000000")
    private BigDecimal minSalary;

    @PositiveOrZero(message = "Mức lương tối đa không được âm")
    @Schema(example = "45000000")
    private BigDecimal maxSalary;

    @Schema(example = "false")
    private Boolean isNegotiable;

    @NotNull(message = "Hạn nộp hồ sơ không được để trống")
    @Future(message = "Hạn nộp hồ sơ phải sau ngày hiện tại")
    @Schema(example = "2026-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate deadline;

    @Schema(example = "[\"Java\", \"Spring Boot\", \"MySQL\"]")
    private List<String> skills;

    @AssertTrue(message = "Mức lương tối thiểu không được lớn hơn mức lương tối đa")
    public boolean isSalaryRangeValid() {
        if (minSalary != null && maxSalary != null) {
            return minSalary.compareTo(maxSalary) <= 0;
        }
        return true;
    }
}
