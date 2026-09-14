package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.CompanyJoinRequest;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;

import java.util.List;
import java.util.Optional;

public interface CompanyJoinRequestRepositoryPort {

    Optional<CompanyJoinRequest> findById(Long id);

    CompanyJoinRequest save(CompanyJoinRequest companyJoinRequest);

    boolean existsByUserIdAndStatus(Long userId, CompanyJoinRequestStatus status);

    List<CompanyJoinRequest> findByCompanyId(Long companyId, CompanyJoinRequestStatus status, int page, int size);

    long countByCompanyId(Long companyId, CompanyJoinRequestStatus status);
}
