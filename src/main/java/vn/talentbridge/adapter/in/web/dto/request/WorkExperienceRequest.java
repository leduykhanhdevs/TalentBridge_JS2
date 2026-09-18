package vn.talentbridge.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkExperienceRequest {

    @NotBlank(message = "Tên công ty không được để trống")
    private String companyName;

    @NotBlank(message = "Vị trí công việc không được để trống")
    private String position;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isCurrent;

    private String description;

    private String achievements;
}
