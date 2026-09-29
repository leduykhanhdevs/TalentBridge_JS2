package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;
import vn.talentbridge.core.application.port.in.GetCompanyJoinRequestsUseCase;
import vn.talentbridge.core.application.port.out.CompanyJoinRequestRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;

import java.util.List;

public class GetCompanyJoinRequestsUseCaseImpl implements GetCompanyJoinRequestsUseCase {

    private final RecruiterRepositoryPort recruiterRepository;
    private final CompanyJoinRequestRepositoryPort companyJoinRequestRepository;

    public GetCompanyJoinRequestsUseCaseImpl(RecruiterRepositoryPort recruiterRepository,
                                            CompanyJoinRequestRepositoryPort companyJoinRequestRepository) {
        this.recruiterRepository = recruiterRepository;
        this.companyJoinRequestRepository = companyJoinRequestRepository;
    }

    @Override
    public List<CompanyJoinRequestResult> getCompanyJoinRequests(Long userId, CompanyJoinRequestStatus status, int page, int size) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống");
        }

        Recruiter recruiter = recruiterRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));

        if (recruiter.getCompany() == null) {
            throw new DomainException(40001, "Nhà tuyển dụng chưa thuộc công ty nào");
        }

        int validPage = Math.max(0, page);
        int validSize = size <= 0 ? 10 : size;

        Long companyId = recruiter.getCompany().getId();
        return companyJoinRequestRepository.findByCompanyId(companyId, status, validPage, validSize)
                .stream()
                .map(CompanyJoinRequestResult::from)
                .toList();
    }

    @Override
    public long countCompanyJoinRequests(Long userId, CompanyJoinRequestStatus status) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống");
        }

        Recruiter recruiter = recruiterRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));

        if (recruiter.getCompany() == null) {
            throw new DomainException(40001, "Nhà tuyển dụng chưa thuộc công ty nào");
        }

        return companyJoinRequestRepository.countByCompanyId(recruiter.getCompany().getId(), status);
    }
}
