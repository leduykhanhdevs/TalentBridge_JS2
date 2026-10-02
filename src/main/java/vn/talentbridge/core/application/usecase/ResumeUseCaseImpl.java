package vn.talentbridge.core.application.usecase;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import vn.talentbridge.core.application.dto.CvTemplateResult;
import vn.talentbridge.core.application.dto.ResumeResult;
import vn.talentbridge.core.application.port.in.ResumeUseCase;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.CvTemplateRepositoryPort;
import vn.talentbridge.core.application.port.out.FileStoragePort;
import vn.talentbridge.core.application.port.out.ResumeRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.CvTemplate;
import vn.talentbridge.core.domain.model.Resume;
import vn.talentbridge.core.domain.model.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;

public class ResumeUseCaseImpl implements ResumeUseCase {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10MB

    private final ResumeRepositoryPort resumeRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final UserRepositoryPort userRepository;
    private final FileStoragePort fileStoragePort;
    private final CvTemplateRepositoryPort cvTemplateRepository;

    public ResumeUseCaseImpl(ResumeRepositoryPort resumeRepository,
                             CandidateRepositoryPort candidateRepository,
                             UserRepositoryPort userRepository,
                             FileStoragePort fileStoragePort,
                             CvTemplateRepositoryPort cvTemplateRepository) {
        this.resumeRepository = resumeRepository;
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.fileStoragePort = fileStoragePort;
        this.cvTemplateRepository = cvTemplateRepository;
    }

    @Override
    public List<CvTemplateResult> getTemplates() {
        return cvTemplateRepository.findAllActive()
                .stream()
                .map(CvTemplateResult::from)
                .toList();
    }

    @Override
    public ResumeResult generateResume(Long userId, String templateCode, String title, String customizationJson, byte[] fileContent) {
        CvTemplate template = cvTemplateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mẫu CV với mã: " + templateCode));

        Candidate candidate = getOrCreateCandidate(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng", userId));

        String effectiveTitle = (title != null && !title.isBlank()) ? title.trim() : ("CV - " + template.name());
        String fileName = "CV_" + sanitizeFileName(user.getFullName() != null ? user.getFullName() : "Candidate") + "_" + templateCode + ".pdf";

        byte[] pdfBytes = fileContent;
        if (pdfBytes == null || pdfBytes.length == 0) {
            pdfBytes = generateDefaultPdf(user, candidate, template);
        }

        String fileUrl = fileStoragePort.storeFile("resumes/" + candidate.getId(), fileName, pdfBytes);
        List<Resume> existing = resumeRepository.findByCandidateId(candidate.getId());
        boolean isDefault = existing.isEmpty();

        Resume resume = new Resume(
                null,
                candidate.getId(),
                template.id(),
                "GENERATED",
                effectiveTitle,
                fileName,
                fileUrl,
                "application/pdf",
                isDefault,
                customizationJson,
                null,
                null
        );

        Resume saved = resumeRepository.save(resume);
        return ResumeResult.from(saved);
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

    private byte[] generateDefaultPdf(User user, Candidate candidate, CvTemplate template) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                // Header background banner
                cs.setNonStrokingColor(30 / 255f, 64 / 255f, 175 / 255f); // Deep blue #1E40AF
                cs.addRect(0, 720, PDRectangle.A4.getWidth(), 122);
                cs.fill();

                // Candidate Name
                cs.setNonStrokingColor(1f, 1f, 1f); // White
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 22);
                cs.newLineAtOffset(50, 785);
                String name = user.getFullName() != null ? stripAccents(user.getFullName()) : "CANDIDATE";
                cs.showText(name);
                cs.endText();

                // Candidate Title
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(50, 762);
                String candidateTitle = candidate.getTitle() != null ? stripAccents(candidate.getTitle()) : "Professional";
                cs.showText(candidateTitle);
                cs.endText();

                // Contact info
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 10);
                cs.newLineAtOffset(50, 735);
                String contact = "Email: " + (user.getEmail() != null ? user.getEmail() : "")
                        + " | Phone: " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A")
                        + " | City: " + (candidate.getCity() != null ? stripAccents(candidate.getCity()) : "Vietnam");
                cs.showText(contact);
                cs.endText();

                // Body text
                cs.setNonStrokingColor(33 / 255f, 37 / 255f, 41 / 255f); // Charcoal
                float currentY = 680;

                // Summary Section
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                cs.newLineAtOffset(50, currentY);
                cs.showText("PROFILE SUMMARY");
                cs.endText();

                currentY -= 20;
                String summary = candidate.getSummary() != null && !candidate.getSummary().isBlank()
                        ? stripAccents(candidate.getSummary())
                        : "Highly motivated professional seeking opportunities to contribute skills and experience.";
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(50, currentY);
                if (summary.length() > 90) {
                    cs.showText(summary.substring(0, Math.min(summary.length(), 90)));
                    cs.newLineAtOffset(0, -14);
                    currentY -= 14;
                    if (summary.length() > 90) {
                        cs.showText(summary.substring(90, Math.min(summary.length(), 180)));
                    }
                } else {
                    cs.showText(summary);
                }
                cs.endText();

                // Template badge
                currentY -= 40;
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(50, currentY);
                cs.showText("TEMPLATE: " + template.name());
                cs.endText();

                // Footer
                cs.setNonStrokingColor(128 / 255f, 128 / 255f, 128 / 255f);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8);
                cs.newLineAtOffset(50, 40);
                cs.showText("Generated by TalentBridge CV Builder - https://talentbridge.vn");
                cs.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi tạo file PDF: " + e.getMessage(), e);
        }
    }

    private String stripAccents(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "")
                .replaceAll("đ", "d")
                .replaceAll("Đ", "D")
                .replaceAll("[^\\x00-\\x7F]", " ");
    }

    private String sanitizeFileName(String name) {
        if (name == null) return "Candidate";
        return stripAccents(name).replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
