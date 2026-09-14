package vn.talentbridge.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
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
public class SubmitJoinCompanyRequest {

    @Size(max = 100, message = "Chức danh không được vượt quá 100 ký tự")
    @Schema(example = "Senior Technical Recruiter", description = "Vị trí / chức danh ứng tuyển vào công ty")
    private String position;

    @Size(max = 1000, message = "Lời nhắn không được vượt quá 1000 ký tự")
    @Schema(example = "Tôi phụ trách tuyển dụng mảng Java Backend cho chi nhánh miền Nam.", description = "Lời nhắn gửi đến ban quản trị / HR công ty")
    private String message;
}
