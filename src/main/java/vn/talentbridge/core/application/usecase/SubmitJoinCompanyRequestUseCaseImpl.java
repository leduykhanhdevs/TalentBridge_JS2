package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;
import vn.talentbridge.core.application.dto.SubmitJoinCompanyRequestCommand;
import vn.talentbridge.core.application.port.in.SubmitJoinCompanyRequestUseCase;
import vn.talentbridge.core.application.port.out.CompanyJoinRequestRepositoryPort;
import vn.talentbridge.core.application.port.out.CompanyRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.CompanyJoinRequest;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;
import vn.talentbridge.core.domain.vo.CompanyStatus;

import java.time.LocalDateTime;

public class SubmitJoinCompanyRequestUseCaseImpl implements SubmitJoinCompanyRequestUseCase {

    private final RecruiterRepositoryPort recruiterRepository;
    private final CompanyRepositoryPort companyRepository;
    private final CompanyJoinRequestRepositoryPort companyJoinRequestRepository;

    public SubmitJoinCompanyRequestUseCaseImpl(RecruiterRepositoryPort recruiterRepository,
                                              CompanyRepositoryPort companyRepository,
                                              CompanyJoinRequestRepositoryPort companyJoinRequestRepository) {
        this.recruiterRepository = recruiterRepository;
        this.companyRepository = companyRepository;
        this.companyJoinRequestRepository = companyJoinRequestRepository;
    }

    @Override
    public CompanyJoinRequestResult submitJoinRequest(Long userId, Long companyId, SubmitJoinCompanyRequestCommand command) {
        Recruiter recruiter = recruiterRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));

        if (recruiter.getCompany() != null) {
            throw new DomainException(40001, "Nhà tuyển dụng đã thuộc về một công ty khác");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty với ID: " + companyId));

        if (company.getStatus() != CompanyStatus.APPROVED) {
            throw new DomainException(40001, "Chỉ có thể gửi yêu cầu gia nhập vào công ty đã được phê duyệt");
        }

        if (companyJoinRequestRepository.existsByUserIdAndStatus(userId, CompanyJoinRequestStatus.PENDING)) {
            throw new DomainException(40901, "Bạn đã có một yêu cầu gia nhập đang chờ xét duyệt");
        }

        CompanyJoinRequest joinRequest = new CompanyJoinRequest();
        joinRequest.setUser(recruiter.getUser());
        joinRequest.setCompany(company);
        String pos = (command.position() != null && !command.position().isBlank())
                ? command.position().trim()
                : recruiter.getPosition();
        joinRequest.setPosition(pos);
        joinRequest.setMessage(command.message() != null ? command.message().trim() : null);
        joinRequest.setStatus(CompanyJoinRequestStatus.PENDING);
        joinRequest.setCreatedAt(LocalDateTime.now());
        joinRequest.setUpdatedAt(LocalDateTime.now());

        CompanyJoinRequest saved = companyJoinRequestRepository.save(joinRequest);
        return CompanyJoinRequestResult.from(saved);
    }
}
