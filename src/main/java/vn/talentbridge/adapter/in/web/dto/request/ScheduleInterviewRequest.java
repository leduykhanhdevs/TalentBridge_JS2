package vn.talentbridge.adapter.in.web.dto.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleInterviewRequest {

    @NotNull(message = "Thời gian phỏng vấn không được để trống")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime interviewTime;

    private String locationType = "ONLINE";

    @NotBlank(message = "Địa điểm hoặc liên kết Google Meet không được để trống")
    private String meetingLinkOrAddress;

    private String notes;

    public static class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (text == null || text.isBlank()) {
                return null;
            }
            text = text.trim();
            try {
                // If text has Z or timezone offset, parse as OffsetDateTime and convert to Vietnam zone
                if (text.endsWith("Z") || text.endsWith("z") || (text.contains("+") && text.indexOf('+') > 10)) {
                    return OffsetDateTime.parse(text).atZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime();
                }
                String normalized = text.replace(" ", "T");
                if (normalized.length() == 16) {
                    return LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                }
                if (normalized.length() >= 19) {
                    return LocalDateTime.parse(normalized.substring(0, 19), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                }
                return LocalDateTime.parse(normalized);
            } catch (Exception e) {
                throw new IllegalArgumentException("Định dạng thời gian phỏng vấn không hợp lệ: " + text, e);
            }
        }
    }
}
