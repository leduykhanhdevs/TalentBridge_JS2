package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.WorkExperienceCommand;
import vn.talentbridge.core.application.dto.WorkExperienceResult;
import vn.talentbridge.core.application.port.in.WorkExperienceUseCase;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.WorkExperienceRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.WorkExperience;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

public class WorkExperienceUseCaseImpl implements WorkExperienceUseCase {

    private final CandidateRepositoryPort candidateRepository;
    private final WorkExperienceRepositoryPort workExperienceRepository;

    public WorkExperienceUseCaseImpl(CandidateRepositoryPort candidateRepository,
                                  WorkExperienceRepositoryPort workExperienceRepository) {
        this.candidateRepository = candidateRepository;
        this.workExperienceRepository = workExperienceRepository;
    }

    @Override
    public List<WorkExperienceResult> getWorkExperiences(Long userId) {
        Candidate candidate = getCandidateByUserId(userId);
        return workExperienceRepository.findByCandidateId(candidate.getId()).stream()
                .map(WorkExperienceResult::from)
                .toList();
    }

    @Override
    public WorkExperienceResult addWorkExperience(Long userId, WorkExperienceCommand command) {
        Candidate candidate = getCandidateByUserId(userId);
        validateCommand(command);

        WorkExperience exp = new WorkExperience(
                null,
                candidate.getId(),
                command.companyName().trim(),
                command.position().trim(),
                command.startDate(),
                Boolean.TRUE.equals(command.isCurrent()) ? null : command.endDate(),
                command.isCurrent(),
                command.description(),
                command.achievements(),
                null,
                null
        );

        WorkExperience saved = workExperienceRepository.save(exp);
        syncExperienceYears(candidate);
        return WorkExperienceResult.from(saved);
    }

    @Override
    public WorkExperienceResult updateWorkExperience(Long userId, Long experienceId, WorkExperienceCommand command) {
        Candidate candidate = getCandidateByUserId(userId);
        validateCommand(command);

        WorkExperience exp = workExperienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Kinh nghiệm làm việc", experienceId));

        if (!exp.getCandidateId().equals(candidate.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa kinh nghiệm làm việc này");
        }

        exp.setCompanyName(command.companyName().trim());
        exp.setPosition(command.position().trim());
        exp.setStartDate(command.startDate());
        exp.setIsCurrent(command.isCurrent());
        exp.setEndDate(Boolean.TRUE.equals(command.isCurrent()) ? null : command.endDate());
        exp.setDescription(command.description());
        exp.setAchievements(command.achievements());

        WorkExperience updated = workExperienceRepository.save(exp);
        syncExperienceYears(candidate);
        return WorkExperienceResult.from(updated);
    }

    @Override
    public void deleteWorkExperience(Long userId, Long experienceId) {
        Candidate candidate = getCandidateByUserId(userId);

        WorkExperience exp = workExperienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Kinh nghiệm làm việc", experienceId));

        if (!exp.getCandidateId().equals(candidate.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền xóa kinh nghiệm làm việc này");
        }

        workExperienceRepository.deleteById(experienceId);
        syncExperienceYears(candidate);
    }

    private Candidate getCandidateByUserId(Long userId) {
        return candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ ứng viên", userId));
    }

    private void validateCommand(WorkExperienceCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Thông tin kinh nghiệm làm việc không hợp lệ");
        }
        if (command.companyName() == null || command.companyName().isBlank()) {
            throw new IllegalArgumentException("Tên công ty không được để trống");
        }
        if (command.position() == null || command.position().isBlank()) {
            throw new IllegalArgumentException("Vị trí làm việc không được để trống");
        }
        if (command.startDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu không được để trống");
        }
        if (command.isCurrent() == null) {
            throw new IllegalArgumentException("Vui lòng xác định công việc hiện tại hay đã kết thúc");
        }
        LocalDate today = LocalDate.now();
        if (command.startDate().isAfter(today)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được ở trong tương lai");
        }
        if (Boolean.FALSE.equals(command.isCurrent()) && command.endDate() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày kết thúc hoặc đánh dấu là Đang làm việc tại đây");
        }
        if (command.endDate() != null && command.endDate().isAfter(today)) {
            throw new IllegalArgumentException("Ngày kết thúc không được ở trong tương lai");
        }
        if (command.endDate() != null && command.endDate().isBefore(command.startDate())) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu");
        }
    }

    private void syncExperienceYears(Candidate candidate) {
        List<WorkExperience> exps = workExperienceRepository.findByCandidateId(candidate.getId());
        int calculatedYears = calculateTotalYears(exps);
        candidate.setExperienceYears(calculatedYears);
        candidateRepository.save(candidate);
    }

    private int calculateTotalYears(List<WorkExperience> experiences) {
        if (experiences == null || experiences.isEmpty()) {
            return 0;
        }
        record Period(LocalDate start, LocalDate end) {}
        List<Period> periods = new ArrayList<>();
        for (WorkExperience exp : experiences) {
            if (exp.getStartDate() == null) continue;
            LocalDate start = exp.getStartDate();
            LocalDate end = Boolean.TRUE.equals(exp.getIsCurrent()) || exp.getEndDate() == null
                    ? LocalDate.now()
                    : exp.getEndDate();
            if (!end.isBefore(start)) {
                periods.add(new Period(start, end));
            }
        }
        if (periods.isEmpty()) return 0;

        periods.sort(Comparator.comparing(Period::start));
        long totalDays = 0;
        LocalDate currentStart = periods.getFirst().start();
        LocalDate currentEnd = periods.getFirst().end();
        for (int i = 1; i < periods.size(); i++) {
            Period next = periods.get(i);
            if (!next.start().isAfter(currentEnd.plusDays(1))) {
                if (next.end().isAfter(currentEnd)) currentEnd = next.end();
            } else {
                totalDays += ChronoUnit.DAYS.between(currentStart, currentEnd.plusDays(1));
                currentStart = next.start();
                currentEnd = next.end();
            }
        }
        totalDays += ChronoUnit.DAYS.between(currentStart, currentEnd.plusDays(1));
        return (int) Math.floor(totalDays / 365.2425d);
    }
}
