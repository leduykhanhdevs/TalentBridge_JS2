package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.application.port.in.UpdateCompanyStatusUseCase;
import vn.talentbridge.core.application.port.out.CompanyJoinRequestRepositoryPort;
import vn.talentbridge.core.application.port.out.CompanyRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;
import vn.talentbridge.core.domain.vo.CompanyStatus;

public class UpdateCompanyStatusUseCaseImpl implements UpdateCompanyStatusUseCase {
    private static final String SYSTEM_CANCELLATION_REASON =
            "Yêu cầu tự hủy: tài khoản đã được liên kết với một công ty.";

    private final CompanyRepositoryPort companyRepository;
    private final RecruiterRepositoryPort recruiterRepository;
    private final CompanyJoinRequestRepositoryPort joinRequestRepository;

    public UpdateCompanyStatusUseCaseImpl(CompanyRepositoryPort companyRepository,
                                          RecruiterRepositoryPort recruiterRepository,
                                          CompanyJoinRequestRepositoryPort joinRequestRepository) {
        this.companyRepository = companyRepository;
        this.recruiterRepository = recruiterRepository;
        this.joinRequestRepository = joinRequestRepository;
    }

    @Override
    public CompanyResult updateCompanyStatus(Long companyId, CompanyStatus status, String reason) {
        if (status == null) throw new DomainException(40001, "Trạng thái doanh nghiệp không hợp lệ");
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Doanh nghiệp", companyId));
        company.setStatus(status);
        Company updated = companyRepository.save(company);

        if (status == CompanyStatus.APPROVED && updated.getCreatedByUserId() != null) {
            recruiterRepository.findByUserId(updated.getCreatedByUserId()).ifPresent(recruiter -> {
                Company linkedCompany = recruiter.getCompany();
                if (linkedCompany != null && !updated.getId().equals(linkedCompany.getId())) {
                    throw new DomainException(40001,
                            "Người tạo doanh nghiệp đã được liên kết với một công ty khác");
                }
                if (linkedCompany == null) {
                    recruiter.setCompany(updated);
                    recruiterRepository.save(recruiter);
                }
                joinRequestRepository.findByUserIdAndStatus(
                                updated.getCreatedByUserId(), CompanyJoinRequestStatus.PENDING)
                        .forEach(request -> {
                            request.cancel(SYSTEM_CANCELLATION_REASON);
                            joinRequestRepository.save(request);
                        });
            });
        }
        return CompanyResult.from(updated);
    }
}
