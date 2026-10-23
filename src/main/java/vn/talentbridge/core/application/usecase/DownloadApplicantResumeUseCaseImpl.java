package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.ApplicantResumeDownload;
import vn.talentbridge.core.application.port.in.DownloadApplicantResumeUseCase;
import vn.talentbridge.core.application.port.out.FileStoragePort;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.port.out.ResumeRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.model.Resume;

public class DownloadApplicantResumeUseCaseImpl implements DownloadApplicantResumeUseCase {
    private final RecruiterRepositoryPort recruiterRepository;
    private final JobRepositoryPort jobRepository;
    private final JobApplicationRepositoryPort applicationRepository;
    private final ResumeRepositoryPort resumeRepository;
    private final FileStoragePort fileStorage;

    public DownloadApplicantResumeUseCaseImpl(
            RecruiterRepositoryPort recruiterRepository,
            JobRepositoryPort jobRepository,
            JobApplicationRepositoryPort applicationRepository,
            ResumeRepositoryPort resumeRepository,
            FileStoragePort fileStorage) {
        this.recruiterRepository = recruiterRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.resumeRepository = resumeRepository;
        this.fileStorage = fileStorage;
    }

    @Override
    public ApplicantResumeDownload downloadForRecruiter(Long recruiterUserId, Long jobId, Long resumeId) {
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));
        if (recruiter.getCompany() == null || recruiter.getCompany().getId() == null) {
            throw new DomainException(40001, "Nhà tuyển dụng chưa thuộc công ty nào");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng", jobId));
        if (!recruiter.getCompany().getId().equals(job.getCompanyId())) {
            throw new DomainException(40301, "Bạn không có quyền xem CV của tin tuyển dụng này");
        }

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ CV", resumeId));
        var application = applicationRepository.findApplicantByJobIdAndCandidateId(jobId, resume.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("CV của ứng viên trong tin tuyển dụng", resumeId));
        if (!resumeId.equals(application.getResumeId())) {
            throw new ResourceNotFoundException("CV đã nộp trong đơn ứng tuyển", resumeId);
        }

        byte[] content = fileStorage.loadFile(resume.getFileUrl());
        return new ApplicantResumeDownload(resume.getFileName(), resume.getFileType(), content);
    }
}
