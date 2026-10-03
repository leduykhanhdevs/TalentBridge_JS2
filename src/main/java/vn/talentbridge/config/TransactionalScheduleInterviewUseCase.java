package vn.talentbridge.config;

import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.core.application.dto.InterviewResult;
import vn.talentbridge.core.application.dto.ScheduleInterviewCommand;
import vn.talentbridge.core.application.port.in.ScheduleInterviewUseCase;

import java.util.List;

public class TransactionalScheduleInterviewUseCase implements ScheduleInterviewUseCase {
    private final ScheduleInterviewUseCase delegate;

    public TransactionalScheduleInterviewUseCase(ScheduleInterviewUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public InterviewResult scheduleInterview(Long recruiterUserId, Long applicationId, ScheduleInterviewCommand command) {
        return delegate.scheduleInterview(recruiterUserId, applicationId, command);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResult> getInterviewsByApplication(Long userId, Long applicationId) {
        return delegate.getInterviewsByApplication(userId, applicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewResult getLatestInterviewForApplication(Long userId, Long applicationId) {
        return delegate.getLatestInterviewForApplication(userId, applicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportIcsCalendar(Long interviewId) {
        return delegate.exportIcsCalendar(interviewId);
    }
}
