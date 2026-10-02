package vn.talentbridge.adapter.in.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleInterviewRequest {

    @NotNull(message = "Thời gian phỏng vấn không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd['T'][' ']HH:mm[:ss]")
    private LocalDateTime interviewTime;

    private String locationType = "ONLINE";

    @NotBlank(message = "Địa điểm hoặc liên kết Google Meet không được để trống")
    private String meetingLinkOrAddress;

    private String notes;
}
