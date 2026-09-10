package vn.talentbridge.modules.company.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.talentbridge.modules.company.entity.Company;
import vn.talentbridge.modules.company.enums.CompanyStatus;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Page<Company> findByStatus(CompanyStatus status, Pageable pageable);
    long countByStatus(CompanyStatus status);
}