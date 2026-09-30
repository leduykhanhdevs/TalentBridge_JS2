package vn.talentbridge.adapter.in.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateAndNoteApplicantRequest {

    @Min(value = 1, message = "Điểm đánh giá tối thiểu là 1 sao")
    @Max(value = 5, message = "Điểm đánh giá tối đa là 5 sao")
    private Integer rating;

    @Size(max = 50, message = "Tag không được vượt quá 50 ký tự")
    private String tag;

    @NotBlank(message = "Nội dung ghi chú không được để trống")
    private String comment;
}
