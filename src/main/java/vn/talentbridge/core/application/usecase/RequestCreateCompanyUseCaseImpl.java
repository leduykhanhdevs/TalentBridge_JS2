package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.application.dto.RequestCreateCompanyCommand;
import vn.talentbridge.core.application.port.in.RequestCreateCompanyUseCase;
import vn.talentbridge.core.application.port.out.CompanyRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.vo.CompanyStatus;

import java.time.LocalDateTime;

public class RequestCreateCompanyUseCaseImpl implements RequestCreateCompanyUseCase {

    private final RecruiterRepositoryPort recruiterRepository;
    private final CompanyRepositoryPort companyRepository;

    public RequestCreateCompanyUseCaseImpl(RecruiterRepositoryPort recruiterRepository,
                                           CompanyRepositoryPort companyRepository) {
        this.recruiterRepository = recruiterRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public CompanyResult requestCreateCompany(Long userId, RequestCreateCompanyCommand command) {
        Recruiter recruiter = recruiterRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));

        if (recruiter.getCompany() != null) {
            throw new DomainException(40001, "Nhà tuyển dụng đã thuộc về một công ty khác");
        }

        if (command.taxCode() != null && !command.taxCode().isBlank()) {
            if (companyRepository.existsByTaxCode(command.taxCode().trim())) {
                throw new DomainException(40901, "Mã số thuế đã tồn tại trên hệ thống");
            }
        }

        Company company = new Company();
        company.setName(command.name() != null ? command.name().trim() : "");
        company.setTaxCode(command.taxCode() != null ? command.taxCode().trim() : null);
        company.setWebsite(command.website() != null ? command.website().trim() : null);
        company.setCompanySize(command.companySize() != null ? command.companySize().trim() : null);
        company.setAddress(command.address() != null ? command.address().trim() : null);
        company.setDescription(command.description() != null ? command.description().trim() : null);
        company.setLogoUrl(command.logoUrl() != null ? command.logoUrl().trim() : null);
        company.setStatus(CompanyStatus.PENDING);
        company.setCreatedByUserId(userId);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        Company savedCompany = companyRepository.save(company);
        return CompanyResult.from(savedCompany);
    }
}
