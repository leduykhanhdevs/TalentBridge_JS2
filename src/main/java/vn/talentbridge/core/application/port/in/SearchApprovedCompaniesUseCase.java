package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CompanyResult;

import java.util.List;

public interface SearchApprovedCompaniesUseCase {
    List<CompanyResult> searchApprovedCompanies(String keyword, int page, int size);
    long countApprovedCompanies(String keyword);
}
