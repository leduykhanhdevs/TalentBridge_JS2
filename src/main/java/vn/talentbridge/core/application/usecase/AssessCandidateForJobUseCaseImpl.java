package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CandidateJobAssessmentResult;
import vn.talentbridge.core.application.dto.CandidateJobMatchResult;
import vn.talentbridge.core.application.dto.ResumeAssessmentStatus;
import vn.talentbridge.core.application.port.in.AssessCandidateForJobUseCase;
import vn.talentbridge.core.application.port.in.MatchApplicantUseCase;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateSkillRepositoryPort;
import vn.talentbridge.core.application.port.out.CvTextExtractionPort;
import vn.talentbridge.core.application.port.out.FileStoragePort;
import vn.talentbridge.core.application.port.out.ResumeRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.Resume;

import java.util.ArrayList;
import java.util.List;

public class AssessCandidateForJobUseCaseImpl implements AssessCandidateForJobUseCase {
    private static final String ADVISORY_NOTICE =
            "Đây là công cụ hỗ trợ xem xét dựa trên dữ liệu hồ sơ/CV được cung cấp; điểm không phải xác suất trúng tuyển và không thay thế quyết định của nhà tuyển dụng.";

    private final MatchApplicantUseCase matchApplicantUseCase;
    private final CandidateRepositoryPort candidateRepository;
    private final CandidateSkillRepositoryPort candidateSkillRepository;
    private final ResumeRepositoryPort resumeRepository;
    private final FileStoragePort fileStorage;
    private final CvTextExtractionPort cvTextExtraction;

    public AssessCandidateForJobUseCaseImpl(
            MatchApplicantUseCase matchApplicantUseCase,
            CandidateRepositoryPort candidateRepository,
            CandidateSkillRepositoryPort candidateSkillRepository,
            ResumeRepositoryPort resumeRepository,
            FileStoragePort fileStorage,
            CvTextExtractionPort cvTextExtraction) {
        this.matchApplicantUseCase = matchApplicantUseCase;
        this.candidateRepository = candidateRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.resumeRepository = resumeRepository;
        this.fileStorage = fileStorage;
        this.cvTextExtraction = cvTextExtraction;
    }

    @Override
    public CandidateJobAssessmentResult assess(Long recruiterUserId, Long jobId, Long candidateId) {
        // Do not read a candidate CV until company ownership and application membership are verified.
        matchApplicantUseCase.assertCanAssessApplicant(recruiterUserId, jobId, candidateId);

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Ứng viên", candidateId));
        List<Resume> resumes = resumeRepository.findByCandidateId(candidateId);
        ResumeText resumeText = loadResumeText(resumes);

        CandidateJobMatchResult match = matchApplicantUseCase.matchApplicantLocally(
                recruiterUserId, jobId, candidateId, resumeText.text());

        List<String> missing = new ArrayList<>();
        int completed = 0;
        if (hasText(candidate.getTitle())) completed++; else missing.add("Chức danh mong muốn");
        if (hasText(candidate.getSummary())) completed++; else missing.add("Tóm tắt hồ sơ");
        if (candidate.getExperienceYears() != null) completed++; else missing.add("Số năm kinh nghiệm");
        if (hasText(candidate.getCity())) completed++; else missing.add("Địa điểm");
        if (!candidateSkillRepository.findByCandidateId(candidateId).isEmpty()) completed++;
        else missing.add("Kỹ năng");
        if (!resumes.isEmpty()) completed++; else missing.add("CV đã tải lên hoặc tạo");

        int completeness = Math.round(completed * 100.0f / 6.0f);
        double score = match.matchPercentage() == null ? 0.0 : match.matchPercentage();
        String assessment = score >= 70.0
                ? (completeness >= 70 ? "Có nhiều điểm khớp trên dữ liệu hiện có; cần xác minh qua phỏng vấn và hồ sơ gốc."
                        : "Có dấu hiệu phù hợp, nhưng hồ sơ thiếu thông tin nên cần xem xét thêm.")
                : score >= 40.0
                        ? "Mức độ khớp trung bình trên dữ liệu hiện có; nên đối chiếu các tiêu chí còn thiếu."
                        : "Chưa thấy nhiều tiêu chí khớp trong dữ liệu hiện có; kiểm tra độ đầy đủ của CV trước khi kết luận.";
        return new CandidateJobAssessmentResult(
                match, completeness, missing, assessment, ADVISORY_NOTICE, resumeText.status());
    }

    private ResumeText loadResumeText(List<Resume> resumes) {
        if (resumes.isEmpty()) return new ResumeText(null, ResumeAssessmentStatus.NO_RESUME);

        // Repository ordering is default resume first, then most recently created.
        Resume resume = resumes.get(0);
        if (!hasText(resume.getFileUrl()) || !hasText(resume.getFileName())) {
            return new ResumeText(null, ResumeAssessmentStatus.CONTENT_UNAVAILABLE);
        }

        try {
            byte[] content = fileStorage.loadFile(resume.getFileUrl());
            String text = cvTextExtraction.extractText(content, resume.getFileName());
            if (!hasText(text)) return new ResumeText(null, ResumeAssessmentStatus.NO_READABLE_TEXT);
            return new ResumeText(text, ResumeAssessmentStatus.EVALUATED);
        } catch (SecurityException securityException) {
            throw securityException;
        } catch (RuntimeException unavailableContent) {
            return new ResumeText(null, ResumeAssessmentStatus.CONTENT_UNAVAILABLE);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record ResumeText(String text, ResumeAssessmentStatus status) {
    }
}
