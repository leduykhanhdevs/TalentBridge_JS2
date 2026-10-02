package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.Resume;

import java.time.LocalDateTime;

public record ResumeResult(
        Long id,
        Long candidateId,
        Integer templateId,
        String title,
        String fileName,
        String fileUrl,
        String fileType,
        String resumeType,
        boolean isDefault,
        String customizationJson,
        LocalDateTime createdAt
) {
    public static ResumeResult from(Resume resume) {
        if (resume == null) return null;
        return new ResumeResult(
                resume.getId(),
                resume.getCandidateId(),
                resume.getTemplateId(),
                resume.getTitle(),
                resume.getFileName(),
                resume.getFileUrl(),
                resume.getFileType(),
                resume.getResumeType(),
                resume.isDefault(),
                resume.getCustomizationJson(),
                resume.getCreatedAt()
        );
    }
}
