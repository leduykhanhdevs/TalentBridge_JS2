package vn.talentbridge.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateApplicantStatusRequest {

    @NotBlank(message = "Trạng thái vòng tuyển dụng không được để trống")
    private String stage;

    private String status;

    private String note;
}
