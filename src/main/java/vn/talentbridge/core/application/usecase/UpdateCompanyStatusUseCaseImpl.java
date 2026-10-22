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
        if (company.isDemo() && status == CompanyStatus.APPROVED) {
            throw new DomainException(40001, "Không thể xác thực hồ sơ doanh nghiệp mô phỏng.");
        }

        if (status == CompanyStatus.APPROVED && company.getCreatedByUserId() != null) {
            recruiterRepository.findByUserId(company.getCreatedByUserId()).ifPresent(recruiter -> {
                Company linkedCompany = recruiter.getCompany();
                if (linkedCompany != null && !company.getId().equals(linkedCompany.getId())) {
                    String linkedCompanyName = linkedCompany.getName() == null
                            ? "khác"
                            : "“" + linkedCompany.getName() + "”";
                    throw new DomainException(40001,
                            "Không thể duyệt: người tạo đang liên kết với công ty "
                                    + linkedCompanyName
                                    + ". Hãy kiểm tra đúng tài khoản hoặc gỡ liên kết hiện tại trước khi duyệt.");
                }
                if (linkedCompany == null) {
                    recruiter.setCompany(company);
                    recruiterRepository.save(recruiter);
                }
                joinRequestRepository.findByUserIdAndStatus(
                                company.getCreatedByUserId(), CompanyJoinRequestStatus.PENDING)
                        .forEach(request -> {
                            request.cancel(SYSTEM_CANCELLATION_REASON);
                            joinRequestRepository.save(request);
                        });
            });
        }

        if (status == CompanyStatus.REJECTED && company.getCreatedByUserId() != null) {
            recruiterRepository.findByUserId(company.getCreatedByUserId()).ifPresent(recruiter -> {
                Company linkedCompany = recruiter.getCompany();
                if (linkedCompany != null && company.getId().equals(linkedCompany.getId())) {
                    recruiter.setCompany(null);
                    recruiterRepository.save(recruiter);
                }
            });
        }

        company.setStatus(status);
        Company updated = companyRepository.save(company);
        return CompanyResult.from(updated);
    }
}
