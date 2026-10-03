package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.port.out.CompanyJoinRequestRepositoryPort;
import vn.talentbridge.core.application.port.out.CompanyRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.UpdateCompanyStatusUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.CompanyJoinRequest;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;
import vn.talentbridge.core.domain.vo.CompanyStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCompanyStatusUseCaseImplTest {
    @Mock CompanyRepositoryPort companyRepository;
    @Mock RecruiterRepositoryPort recruiterRepository;
    @Mock CompanyJoinRequestRepositoryPort joinRequestRepository;
    private UpdateCompanyStatusUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCompanyStatusUseCaseImpl(companyRepository, recruiterRepository, joinRequestRepository);
    }

    @Test
    void adminApprovalLinksCreatorAndCancelsOtherPendingJoinRequests() {
        Company company = new Company();
        company.setId(10L);
        company.setName("Created Company");
        company.setCreatedByUserId(20L);
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenAnswer(call -> call.getArgument(0));

        Recruiter creator = new Recruiter();
        creator.setId(30L);
        CompanyJoinRequest pending = new CompanyJoinRequest();
        pending.setId(40L);
        pending.setStatus(CompanyJoinRequestStatus.PENDING);
        when(recruiterRepository.findByUserId(20L)).thenReturn(Optional.of(creator));
        when(joinRequestRepository.findByUserIdAndStatus(20L, CompanyJoinRequestStatus.PENDING))
                .thenReturn(List.of(pending));
        when(joinRequestRepository.save(any(CompanyJoinRequest.class))).thenAnswer(call -> call.getArgument(0));

        var result = useCase.updateCompanyStatus(10L, CompanyStatus.APPROVED, "Đã xác minh");

        assertThat(result.status()).isEqualTo("APPROVED");
        assertThat(creator.getCompany()).isSameAs(company);
        verify(recruiterRepository).save(creator);
        assertThat(pending.getStatus()).isEqualTo(CompanyJoinRequestStatus.CANCELLED);
        assertThat(pending.getReason()).contains("đã được liên kết");
        verify(joinRequestRepository).save(pending);
    }

    @Test
    void doesNotReplaceExistingCompanyLinkWhenAdminApprovesAnotherCompany() {
        Company candidateCompany = new Company();
        candidateCompany.setId(10L);
        candidateCompany.setCreatedByUserId(20L);
        Company existingCompany = new Company();
        existingCompany.setId(11L);
        Recruiter creator = new Recruiter();
        creator.setCompany(existingCompany);
        when(companyRepository.findById(10L)).thenReturn(Optional.of(candidateCompany));
        when(companyRepository.save(any(Company.class))).thenAnswer(call -> call.getArgument(0));
        when(recruiterRepository.findByUserId(20L)).thenReturn(Optional.of(creator));

        assertThatThrownBy(() -> useCase.updateCompanyStatus(10L, CompanyStatus.APPROVED, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("đã được liên kết");

        assertThat(creator.getCompany()).isSameAs(existingCompany);
        verify(recruiterRepository, never()).save(any());
        verifyNoInteractions(joinRequestRepository);
    }
}
