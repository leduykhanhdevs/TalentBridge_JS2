package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.application.port.in.SearchApprovedCompaniesUseCase;
import vn.talentbridge.core.application.port.out.CompanyRepositoryPort;
import vn.talentbridge.core.domain.vo.CompanyStatus;

import java.util.List;

public class SearchApprovedCompaniesUseCaseImpl implements SearchApprovedCompaniesUseCase {

    private final CompanyRepositoryPort companyRepository;

    public SearchApprovedCompaniesUseCaseImpl(CompanyRepositoryPort companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public List<CompanyResult> searchApprovedCompanies(String keyword, int page, int size) {
        return companyRepository.findAll(page, size, keyword, CompanyStatus.APPROVED)
                .stream()
                .map(CompanyResult::from)
                .toList();
    }

    @Override
    public long countApprovedCompanies(String keyword) {
        return companyRepository.count(keyword, CompanyStatus.APPROVED);
    }
}
