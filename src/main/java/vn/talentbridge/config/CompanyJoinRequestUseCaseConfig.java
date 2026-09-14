package vn.talentbridge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.talentbridge.core.application.port.in.GetCompanyJoinRequestsUseCase;
import vn.talentbridge.core.application.port.in.ReviewJoinRequestUseCase;
import vn.talentbridge.core.application.port.in.SearchApprovedCompaniesUseCase;
import vn.talentbridge.core.application.port.in.SubmitJoinCompanyRequestUseCase;
import vn.talentbridge.core.application.port.out.CompanyJoinRequestRepositoryPort;
import vn.talentbridge.core.application.port.out.CompanyRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.GetCompanyJoinRequestsUseCaseImpl;
import vn.talentbridge.core.application.usecase.ReviewJoinRequestUseCaseImpl;
import vn.talentbridge.core.application.usecase.SearchApprovedCompaniesUseCaseImpl;
import vn.talentbridge.core.application.usecase.SubmitJoinCompanyRequestUseCaseImpl;

@Configuration
public class CompanyJoinRequestUseCaseConfig {

    @Bean
    public SearchApprovedCompaniesUseCase searchApprovedCompaniesUseCase(
            CompanyRepositoryPort companyRepository) {
        return new SearchApprovedCompaniesUseCaseImpl(companyRepository);
    }

    @Bean
    public SubmitJoinCompanyRequestUseCase submitJoinCompanyRequestUseCase(
            RecruiterRepositoryPort recruiterRepository,
            CompanyRepositoryPort companyRepository,
            CompanyJoinRequestRepositoryPort companyJoinRequestRepository) {
        return new SubmitJoinCompanyRequestUseCaseImpl(recruiterRepository, companyRepository, companyJoinRequestRepository);
    }

    @Bean
    public GetCompanyJoinRequestsUseCase getCompanyJoinRequestsUseCase(
            RecruiterRepositoryPort recruiterRepository,
            CompanyJoinRequestRepositoryPort companyJoinRequestRepository) {
        return new GetCompanyJoinRequestsUseCaseImpl(recruiterRepository, companyJoinRequestRepository);
    }

    @Bean
    public ReviewJoinRequestUseCase reviewJoinRequestUseCase(
            RecruiterRepositoryPort recruiterRepository,
            CompanyJoinRequestRepositoryPort companyJoinRequestRepository) {
        return new ReviewJoinRequestUseCaseImpl(recruiterRepository, companyJoinRequestRepository);
    }
}
