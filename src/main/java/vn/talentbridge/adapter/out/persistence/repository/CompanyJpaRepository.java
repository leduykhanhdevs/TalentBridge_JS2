package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.core.domain.vo.CompanyStatus;

@Repository
public interface CompanyJpaRepository extends JpaRepository<CompanyJpaEntity, Long> {

    @Query("SELECT c FROM CompanyJpaEntity c " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.taxCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR c.status = :status)")
    Page<CompanyJpaEntity> searchCompanies(@Param("keyword") String keyword,
                                           @Param("status") CompanyStatus status,
                                           Pageable pageable);

    @Query("SELECT COUNT(c) FROM CompanyJpaEntity c " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.taxCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR c.status = :status)")
    long countSearchCompanies(@Param("keyword") String keyword,
                              @Param("status") CompanyStatus status);

    Page<CompanyJpaEntity> findByStatus(CompanyStatus status, Pageable pageable);
    long countByStatus(CompanyStatus status);
}