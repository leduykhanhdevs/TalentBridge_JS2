package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;
import vn.talentbridge.core.application.dto.ReviewJoinRequestCommand;
import vn.talentbridge.core.application.port.in.ReviewJoinRequestUseCase;
import vn.talentbridge.core.application.port.out.CompanyJoinRequestRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.CompanyJoinRequest;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;

public class ReviewJoinRequestUseCaseImpl implements ReviewJoinRequestUseCase {

    private final RecruiterRepositoryPort recruiterRepository;
    private final CompanyJoinRequestRepositoryPort companyJoinRequestRepository;

    public ReviewJoinRequestUseCaseImpl(RecruiterRepositoryPort recruiterRepository,
                                       CompanyJoinRequestRepositoryPort companyJoinRequestRepository) {
        this.recruiterRepository = recruiterRepository;
        this.companyJoinRequestRepository = companyJoinRequestRepository;
    }

    @Override
    public CompanyJoinRequestResult reviewJoinRequest(Long reviewerUserId, Long requestId, ReviewJoinRequestCommand command) {
        if (reviewerUserId == null || requestId == null || command == null) {
            throw new IllegalArgumentException("Thông tin xét duyệt yêu cầu gia nhập không hợp lệ");
        }

        Recruiter reviewer = recruiterRepository.findByUserId(reviewerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));

        if (reviewer.getCompany() == null) {
            throw new DomainException(40001, "Nhà tuyển dụng chưa thuộc công ty nào");
        }

        CompanyJoinRequest joinRequest = companyJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu gia nhập với ID: " + requestId));

        Long reviewerCompanyId = reviewer.getCompany().getId();
        Long requestCompanyId = joinRequest.getCompany() != null ? joinRequest.getCompany().getId() : null;

        if (!reviewerCompanyId.equals(requestCompanyId)) {
            throw new DomainException(40301, "Bạn không có quyền thao tác trên yêu cầu gia nhập của công ty khác");
        }

        if (joinRequest.getStatus() != CompanyJoinRequestStatus.PENDING) {
            throw new DomainException(40001, "Yêu cầu gia nhập đã được xử lý trước đó");
        }

        if (command.status() == CompanyJoinRequestStatus.ACCEPTED
                && joinRequest.getUser() != null
                && joinRequest.getUser().getId() != null) {
            Recruiter applicant = recruiterRepository.findByUserId(joinRequest.getUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));
            if (applicant.getCompany() != null) {
                throw new DomainException(40001, "Nhà tuyển dụng đã được liên kết với một công ty khác");
            }
        }

        if (command.status() == null || (command.status() != CompanyJoinRequestStatus.ACCEPTED && command.status() != CompanyJoinRequestStatus.REJECTED)) {
            throw new DomainException(40001, "Trạng thái phê duyệt chỉ được là ACCEPTED hoặc REJECTED");
        }

        if (command.status() == CompanyJoinRequestStatus.ACCEPTED) {
            joinRequest.accept(reviewerUserId, command.reason());
            CompanyJoinRequest saved = companyJoinRequestRepository.save(joinRequest);

            if (joinRequest.getUser() != null && joinRequest.getUser().getId() != null) {
                Recruiter applicant = recruiterRepository.findByUserId(joinRequest.getUser().getId()).orElseThrow();
                applicant.setCompany(reviewer.getCompany());
                if (joinRequest.getPosition() != null && !joinRequest.getPosition().isBlank()) {
                    applicant.setPosition(joinRequest.getPosition().trim());
                }
                recruiterRepository.save(applicant);
                companyJoinRequestRepository.findByUserIdAndStatus(joinRequest.getUser().getId(), CompanyJoinRequestStatus.PENDING)
                        .stream()
                        .filter(other -> !other.getId().equals(joinRequest.getId()))
                        .forEach(other -> {
                            other.cancel("Yêu cầu tự hủy: tài khoản đã được liên kết với một công ty.");
                            companyJoinRequestRepository.save(other);
                        });
            }

            return CompanyJoinRequestResult.from(saved);
        } else {
            joinRequest.reject(reviewerUserId, command.reason());
            CompanyJoinRequest saved = companyJoinRequestRepository.save(joinRequest);
            return CompanyJoinRequestResult.from(saved);
        }
    }
}
