package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.Interview;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public record InterviewResult(
        Long id,
        Long applicationId,
        Long jobId,
        String jobTitle,
        String companyName,
        String candidateName,
        String candidateEmail,
        LocalDateTime interviewTime,
        String locationType,
        String meetingLinkOrAddress,
        String notes,
        String status,
        String googleCalendarUrl,
        LocalDateTime createdAt
) {
    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public static InterviewResult from(
            Interview interview,
            Long jobId,
            String jobTitle,
            String companyName,
            String candidateName,
            String candidateEmail) {

        if (interview == null) return null;

        String calendarUrl = buildGoogleCalendarUrl(
                interview.getInterviewTime(),
                jobTitle != null ? jobTitle : "Phỏng vấn",
                companyName != null ? companyName : "Nhà tuyển dụng",
                candidateName != null ? candidateName : "Ứng viên",
                candidateEmail != null ? candidateEmail : "",
                interview.getMeetingLinkOrAddress(),
                interview.getNotes()
        );

        return new InterviewResult(
                interview.getId(),
                interview.getApplicationId(),
                jobId,
                jobTitle != null ? jobTitle : "Phỏng vấn",
                companyName != null ? companyName : "Nhà tuyển dụng",
                candidateName != null ? candidateName : "Ứng viên",
                candidateEmail != null ? candidateEmail : "",
                interview.getInterviewTime(),
                interview.getLocationType(),
                interview.getMeetingLinkOrAddress(),
                interview.getNotes(),
                interview.getStatus(),
                calendarUrl,
                interview.getCreatedAt()
        );
    }

    public static String buildGoogleCalendarUrl(
            LocalDateTime interviewTime,
            String jobTitle,
            String companyName,
            String candidateName,
            String candidateEmail,
            String locationOrLink,
            String notes) {

        if (interviewTime == null) return "";

        try {
            LocalDateTime start = interviewTime;
            LocalDateTime end = interviewTime.plusHours(1);

            String startUtc = start.atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .format(UTC_FORMATTER);
            String endUtc = end.atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .format(UTC_FORMATTER);

            String eventTitle = URLEncoder.encode("Phỏng vấn: " + jobTitle + " - " + companyName, StandardCharsets.UTF_8);
            String detailsText = "Lịch phỏng vấn TalentBridge"
                    + "\n- Vị trí: " + jobTitle
                    + "\n- Công ty: " + companyName
                    + "\n- Ứng viên: " + candidateName
                    + (notes != null && !notes.isBlank() ? "\n- Ghi chú: " + notes : "")
                    + "\n- Địa điểm / Meeting Link: " + locationOrLink;

            String details = URLEncoder.encode(detailsText, StandardCharsets.UTF_8);
            String location = URLEncoder.encode(locationOrLink != null ? locationOrLink : "", StandardCharsets.UTF_8);

            String url = "https://calendar.google.com/calendar/render?action=TEMPLATE"
                    + "&text=" + eventTitle
                    + "&dates=" + startUtc + "/" + endUtc
                    + "&details=" + details
                    + "&location=" + location;

            if (candidateEmail != null && !candidateEmail.isBlank()) {
                url += "&add=" + URLEncoder.encode(candidateEmail, StandardCharsets.UTF_8);
            }

            return url;
        } catch (Exception e) {
            return "https://calendar.google.com/calendar/render";
        }
    }
}
