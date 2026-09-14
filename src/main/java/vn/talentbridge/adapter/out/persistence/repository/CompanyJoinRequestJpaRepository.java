package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJoinRequestJpaEntity;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;

@Repository
public interface CompanyJoinRequestJpaRepository extends JpaRepository<CompanyJoinRequestJpaEntity, Long> {

    boolean existsByUserIdAndStatus(Long userId, CompanyJoinRequestStatus status);

    Page<CompanyJoinRequestJpaEntity> findByCompanyIdAndStatus(Long companyId, CompanyJoinRequestStatus status, Pageable pageable);

    Page<CompanyJoinRequestJpaEntity> findByCompanyId(Long companyId, Pageable pageable);

    long countByCompanyIdAndStatus(Long companyId, CompanyJoinRequestStatus status);

    long countByCompanyId(Long companyId);
}
