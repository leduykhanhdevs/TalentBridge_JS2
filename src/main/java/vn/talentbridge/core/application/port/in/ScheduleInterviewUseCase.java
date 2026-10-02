package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.InterviewResult;
import vn.talentbridge.core.application.dto.ScheduleInterviewCommand;

import java.util.List;

public interface ScheduleInterviewUseCase {

    InterviewResult scheduleInterview(Long recruiterUserId, Long applicationId, ScheduleInterviewCommand command);

    List<InterviewResult> getInterviewsByApplication(Long userId, Long applicationId);

    InterviewResult getLatestInterviewForApplication(Long userId, Long applicationId);

    byte[] exportIcsCalendar(Long interviewId);
}
