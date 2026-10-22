package vn.talentbridge.adapter.in.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateSkillRequest {

    private Integer skillId;

    private String skillName;

    private String proficiencyLevel;

    @Min(value = 1, message = "Đánh giá tối thiểu 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa 5 sao")
    private Integer rating;

    @jakarta.validation.constraints.DecimalMin(value = "0.1", message = "Số năm kinh nghiệm tối thiểu là 0.1 năm")
    @jakarta.validation.constraints.DecimalMax(value = "50.0", message = "Số năm kinh nghiệm không được vượt quá 50 năm")
    private Double yearsOfExperience;
}
