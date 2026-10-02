package vn.talentbridge.core.application.dto;

import java.time.LocalDateTime;

public record ScheduleInterviewCommand(
        LocalDateTime interviewTime,
        String locationType,
        String meetingLinkOrAddress,
        String notes
) {
}
