package vn.talentbridge.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplyJobRequest {

    @NotNull(message = "Vui lòng chọn công việc")
    @Positive(message = "Mã công việc không hợp lệ")
    private Long jobId;

    @NotNull(message = "Vui lòng chọn CV")
    @Positive(message = "Mã CV không hợp lệ")
    private Long resumeId;

    private String coverLetter;
}