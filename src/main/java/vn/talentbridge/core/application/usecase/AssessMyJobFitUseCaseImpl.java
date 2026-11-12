package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CandidateJobAssessmentResult;
import vn.talentbridge.core.application.dto.CandidateJobMatchResult;
import vn.talentbridge.core.application.dto.ResumeAssessmentStatus;
import vn.talentbridge.core.application.port.in.AssessMyJobFitUseCase;
import vn.talentbridge.core.application.port.out.CandidateJobMatchingPort;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateSkillRepositoryPort;
import vn.talentbridge.core.application.port.out.CvTextExtractionPort;
import vn.talentbridge.core.application.port.out.FileStoragePort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.ResumeRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.Resume;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.util.ArrayList;
import java.util.List;

public class AssessMyJobFitUseCaseImpl implements AssessMyJobFitUseCase {
    private static final String ADVISORY_NOTICE =
            "Đây là phép so khớp tham khảo giữa hồ sơ/CV và yêu cầu công việc; điểm không phải xác suất được tuyển và không thay thế quyết định của bạn.";

    private final CandidateJobMatchingPort matchingPort;
    private final JobRepositoryPort jobRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final CandidateSkillRepositoryPort candidateSkillRepository;
    private final ResumeRepositoryPort resumeRepository;
    private final FileStoragePort fileStorage;
    private final CvTextExtractionPort cvTextExtraction;

    public AssessMyJobFitUseCaseImpl(
            CandidateJobMatchingPort matchingPort,
            JobRepositoryPort jobRepository,
            CandidateRepositoryPort candidateRepository,
            CandidateSkillRepositoryPort candidateSkillRepository,
            ResumeRepositoryPort resumeRepository,
            FileStoragePort fileStorage,
            CvTextExtractionPort cvTextExtraction) {
        this.matchingPort = matchingPort;
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.resumeRepository = resumeRepository;
        this.fileStorage = fileStorage;
        this.cvTextExtraction = cvTextExtraction;
    }

    @Override
    public CandidateJobAssessmentResult assess(Long candidateUserId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .filter(found -> found.getStatus() == JobStatus.ACTIVE && !found.isDemo())
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng", jobId));
        Candidate candidate = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ ứng viên"));

        List<Resume> resumes = resumeRepository.findByCandidateId(candidate.getId());
        Resume selectedResume = resumeRepository.findDefaultByCandidateId(candidate.getId())
                .orElseGet(() -> resumes.isEmpty() ? null : resumes.get(0));
        ResumeText resumeText = loadResumeText(selectedResume);
        CandidateJobMatchResult match = matchingPort.matchLocally(job.getId(), candidate.getId(), resumeText.text());

        List<String> missing = new ArrayList<>();
        int completed = 0;
        if (hasText(candidate.getTitle())) completed++; else missing.add("Chức danh mong muốn");
        if (hasText(candidate.getSummary())) completed++; else missing.add("Tóm tắt hồ sơ");
        if (candidate.getExperienceYears() != null) completed++; else missing.add("Số năm kinh nghiệm");
        if (hasText(candidate.getCity())) completed++; else missing.add("Địa điểm");
        if (!candidateSkillRepository.findByCandidateId(candidate.getId()).isEmpty()) completed++;
        else missing.add("Kỹ năng");
        if (selectedResume != null) completed++; else missing.add("CV đã tải lên hoặc tạo");

        int completeness = Math.round(completed * 100.0f / 6.0f);
        double score = match.matchPercentage() == null ? 0.0 : match.matchPercentage();
        String overall = score >= 70.0
                ? (completeness >= 70 ? "Hồ sơ có nhiều tiêu chí khớp; hãy đối chiếu trực tiếp với yêu cầu tuyển dụng."
                        : "Có dấu hiệu phù hợp nhưng hồ sơ chưa đầy đủ; bổ sung thông tin để kết quả sát hơn.")
                : score >= 40.0
                        ? "Mức độ khớp trung bình theo dữ liệu hiện có; xem các kỹ năng còn thiếu trước khi ứng tuyển."
                        : "Chưa có nhiều tiêu chí khớp trong dữ liệu hiện có; hãy kiểm tra yêu cầu và hồ sơ của bạn.";
        return new CandidateJobAssessmentResult(match, completeness, missing, overall, ADVISORY_NOTICE, resumeText.status());
    }

    private ResumeText loadResumeText(Resume resume) {
        if (resume == null) return new ResumeText(null, ResumeAssessmentStatus.NO_RESUME);
        if (!hasText(resume.getFileUrl()) || !hasText(resume.getFileName())) {
            return new ResumeText(null, ResumeAssessmentStatus.CONTENT_UNAVAILABLE);
        }
        try {
            String text = cvTextExtraction.extractText(fileStorage.loadFile(resume.getFileUrl()), resume.getFileName());
            return hasText(text)
                    ? new ResumeText(text, ResumeAssessmentStatus.EVALUATED)
                    : new ResumeText(null, ResumeAssessmentStatus.NO_READABLE_TEXT);
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
