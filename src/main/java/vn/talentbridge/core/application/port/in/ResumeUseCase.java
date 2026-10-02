package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CvTemplateResult;
import vn.talentbridge.core.application.dto.ResumeResult;

import java.util.List;

public interface ResumeUseCase {

    ResumeResult uploadResume(Long userId, String title, String originalFileName, String contentType, byte[] content);

    ResumeResult generateResume(Long userId, String templateCode, String title, String customizationJson, byte[] fileContent);

    List<ResumeResult> getResumes(Long userId);

    List<CvTemplateResult> getTemplates();

    void deleteResume(Long userId, Long resumeId);

    ResumeResult setDefaultResume(Long userId, Long resumeId);

    byte[] downloadResume(Long userId, Long resumeId);

    ResumeResult getResume(Long userId, Long resumeId);
}

