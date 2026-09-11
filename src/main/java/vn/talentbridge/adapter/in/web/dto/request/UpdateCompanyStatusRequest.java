package vn.talentbridge.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.talentbridge.core.domain.vo.CompanyStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompanyStatusRequest {

    @NotNull(message = "Trạng thái doanh nghiệp không được để trống")
    private CompanyStatus status;

    private String reason;
}