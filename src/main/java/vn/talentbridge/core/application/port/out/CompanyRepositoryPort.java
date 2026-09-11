package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.vo.CompanyStatus;

import java.util.List;
import java.util.Optional;

public interface CompanyRepositoryPort {
    Optional<Company> findById(Long id);
    Company save(Company company);
    List<Company> findAll(int page, int size, String keyword, CompanyStatus status);
    long count(String keyword, CompanyStatus status);
    long count();
    long countByStatus(CompanyStatus status);
}