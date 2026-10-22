package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.RequestCreateCompanyCommand;
import vn.talentbridge.core.application.port.out.CompanyRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.RequestCreateCompanyUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.vo.CompanyStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestCreateCompanyUseCaseImplTest {

    @Mock
    private RecruiterRepositoryPort recruiterRepository;

    @Mock
    private CompanyRepositoryPort companyRepository;

    private RequestCreateCompanyUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new RequestCreateCompanyUseCaseImpl(
                recruiterRepository,
                companyRepository
        );
    }

    @Test
    void createsPendingCompanyAndLinksItToRecruiter() {
        Recruiter recruiter = new Recruiter();
        when(recruiterRepository.findByUserId(20L)).thenReturn(Optional.of(recruiter));
        when(companyRepository.findPendingByCreatedByUserId(20L))
                .thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
            Company company = invocation.getArgument(0);
            company.setId(10L);
            return company;
        });
        when(recruiterRepository.save(any(Recruiter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.requestCreateCompany(
                20L,
                new RequestCreateCompanyCommand(
                        "  TalentBridge Labs  ", null, null, null,
                        null, null, null, null
                )
        );

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.name()).isEqualTo("TalentBridge Labs");
        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(companyCaptor.capture());
        assertThat(companyCaptor.getValue().getCreatedByUserId()).isEqualTo(20L);
        assertThat(companyCaptor.getValue().getStatus()).isEqualTo(CompanyStatus.PENDING);
        assertThat(recruiter.getCompany()).isSameAs(companyCaptor.getValue());
        verify(recruiterRepository).save(recruiter);
    }

    @Test
    void reattachesExistingLegacyPendingCompanyInsteadOfCreatingDuplicate() {
        Recruiter recruiter = new Recruiter();
        Company pending = new Company();
        pending.setId(10L);
        pending.setName("Legacy Request");
        pending.setCreatedByUserId(20L);
        pending.setStatus(CompanyStatus.PENDING);
        when(recruiterRepository.findByUserId(20L)).thenReturn(Optional.of(recruiter));
        when(companyRepository.findPendingByCreatedByUserId(20L))
                .thenReturn(Optional.of(pending));
        when(recruiterRepository.save(any(Recruiter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.requestCreateCompany(
                20L,
                new RequestCreateCompanyCommand("Another name", null, null,
                        null, null, null, null, null)
        );

        assertThat(result.id()).isEqualTo(10L);
        assertThat(recruiter.getCompany()).isSameAs(pending);
        verify(companyRepository, never()).save(any());
        verify(recruiterRepository).save(recruiter);
    }

    @Test
    void rejectsCreatingAnotherCompanyWhenRecruiterAlreadyHasACompany() {
        Recruiter recruiter = new Recruiter();
        recruiter.setCompany(new Company());
        when(recruiterRepository.findByUserId(20L)).thenReturn(Optional.of(recruiter));

        assertThatThrownBy(() -> useCase.requestCreateCompany(
                20L,
                new RequestCreateCompanyCommand("Another company", null, null,
                        null, null, null, null, null)
        )).isInstanceOf(DomainException.class)
                .hasMessageContaining("đã thuộc về một công ty khác");

        verifyNoInteractions(companyRepository);
        verify(recruiterRepository, never()).save(any());
    }
}
