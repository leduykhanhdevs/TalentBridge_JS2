package vn.talentbridge.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @NotNull(message = "Mã ứng viên không được để trống")
    private Long candidateId;

    @Deprecated
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String rawCvText;
}
