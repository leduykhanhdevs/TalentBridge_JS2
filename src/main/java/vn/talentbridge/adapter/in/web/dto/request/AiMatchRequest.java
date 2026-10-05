package vn.talentbridge.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMatchRequest {

    @NotNull(message = "Mã công việc không được để trống")
    private Long jobId;

    private Long candidateId;

    private String rawCvText;
}
