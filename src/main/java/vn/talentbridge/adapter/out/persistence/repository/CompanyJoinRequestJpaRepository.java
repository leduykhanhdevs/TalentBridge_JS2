package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJoinRequestJpaEntity;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;

import java.util.List;

import java.util.Optional;

@Repository
public interface CompanyJoinRequestJpaRepository extends JpaRepository<CompanyJoinRequestJpaEntity, Long> {

    boolean existsByUserIdAndStatus(Long userId, CompanyJoinRequestStatus status);

    Optional<CompanyJoinRequestJpaEntity> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, CompanyJoinRequestStatus status);

    Page<CompanyJoinRequestJpaEntity> findByCompanyIdAndStatus(Long companyId, CompanyJoinRequestStatus status, Pageable pageable);

    List<CompanyJoinRequestJpaEntity> findByUserIdAndStatus(Long userId, CompanyJoinRequestStatus status);

    Page<CompanyJoinRequestJpaEntity> findByCompanyId(Long companyId, Pageable pageable);

    long countByCompanyIdAndStatus(Long companyId, CompanyJoinRequestStatus status);

    long countByCompanyId(Long companyId);
}
