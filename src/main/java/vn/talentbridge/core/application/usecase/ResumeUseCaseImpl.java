package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.ResumeResult;
import vn.talentbridge.core.application.port.in.ResumeUseCase;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.FileStoragePort;
import vn.talentbridge.core.application.port.out.ResumeRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.Resume;
import vn.talentbridge.core.domain.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class ResumeUseCaseImpl implements ResumeUseCase {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10MB

    private final ResumeRepositoryPort resumeRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final UserRepositoryPort userRepository;
    private final FileStoragePort fileStoragePort;

    public ResumeUseCaseImpl(ResumeRepositoryPort resumeRepository,
                             CandidateRepositoryPort candidateRepository,
                             UserRepositoryPort userRepository,
                             FileStoragePort fileStoragePort) {
        this.resumeRepository = resumeRepository;
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.fileStoragePort = fileStoragePort;
    }

    @Override
    public ResumeResult uploadResume(Long userId, String title, String originalFileName, String contentType, byte[] content) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Vui lòng chọn file CV để tải lên.");
        }
        if (content.length > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Dung lượng file CV không được vượt quá 10MB.");
        }
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Tên file không hợp lệ.");
        }

        String lowerName = originalFileName.toLowerCase().trim();
        String fileType;
        if (lowerName.endsWith(".pdf")) {
            fileType = "application/pdf";
        } else if (lowerName.endsWith(".docx")) {
            fileType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else {
            throw new IllegalArgumentException("Định dạng file không hợp lệ. Hệ thống chỉ hỗ trợ file PDF (.pdf) hoặc Word (.docx).");
        }

        Candidate candidate = getOrCreateCandidate(userId);
        List<Resume> existing = resumeRepository.findByCandidateId(candidate.getId());
        boolean isDefault = existing.isEmpty();

        String effectiveTitle = (title != null && !title.isBlank()) ? title.trim() : originalFileName;
        String fileUrl = fileStoragePort.storeFile("resumes/" + candidate.getId(), originalFileName, content);

        Resume resume = new Resume(
                null,
                candidate.getId(),
                null,
                "UPLOADED",
                effectiveTitle,
                originalFileName,
                fileUrl,
                fileType,
                isDefault,
                null,
                null
        );

        Resume saved = resumeRepository.save(resume);
        return ResumeResult.from(saved);
    }

    @Override
    public List<ResumeResult> getResumes(Long userId) {
        Candidate candidate = getOrCreateCandidate(userId);
        return resumeRepository.findByCandidateId(candidate.getId())
                .stream()
                .map(ResumeResult::from)
                .toList();
    }

    @Override
    public void deleteResume(Long userId, Long resumeId) {
        Candidate candidate = getOrCreateCandidate(userId);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ CV", resumeId));

        if (!resume.getCandidateId().equals(candidate.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền xóa hồ sơ CV này.");
        }

        fileStoragePort.deleteFile(resume.getFileUrl());
        resumeRepository.delete(resumeId);

        if (resume.isDefault()) {
            List<Resume> remaining = resumeRepository.findByCandidateId(candidate.getId());
            if (!remaining.isEmpty()) {
                Resume nextDefault = remaining.get(0);
                nextDefault.setDefault(true);
                resumeRepository.save(nextDefault);
            }
        }
    }

    @Override
    public ResumeResult setDefaultResume(Long userId, Long resumeId) {
        Candidate candidate = getOrCreateCandidate(userId);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ CV", resumeId));

        if (!resume.getCandidateId().equals(candidate.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác trên hồ sơ CV này.");
        }

        resumeRepository.clearDefault(candidate.getId());
        resume.setDefault(true);
        Resume saved = resumeRepository.save(resume);
        return ResumeResult.from(saved);
    }

    @Override
    public byte[] downloadResume(Long userId, Long resumeId) {
        Candidate candidate = getOrCreateCandidate(userId);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ CV", resumeId));

        if (!resume.getCandidateId().equals(candidate.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền tải xuống hồ sơ CV này.");
        }

        return fileStoragePort.loadFile(resume.getFileUrl());
    }

    @Override
    public ResumeResult getResume(Long userId, Long resumeId) {
        Candidate candidate = getOrCreateCandidate(userId);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ CV", resumeId));

        if (!resume.getCandidateId().equals(candidate.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền xem hồ sơ CV này.");
        }

        return ResumeResult.from(resume);
    }

    private Candidate getOrCreateCandidate(Long userId) {
        return candidateRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng", userId));
                    Candidate newCandidate = new Candidate();
                    newCandidate.setUser(user);
                    newCandidate.setCreatedAt(LocalDateTime.now());
                    newCandidate.setUpdatedAt(LocalDateTime.now());
                    return candidateRepository.save(newCandidate);
                });
    }
}
