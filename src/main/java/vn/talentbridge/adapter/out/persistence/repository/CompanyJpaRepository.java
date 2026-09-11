package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.core.domain.vo.CompanyStatus;

@Repository
public interface CompanyJpaRepository extends JpaRepository<CompanyJpaEntity, Long> {
    Page<CompanyJpaEntity> findByStatus(CompanyStatus status, Pageable pageable);
    long countByStatus(CompanyStatus status);
}