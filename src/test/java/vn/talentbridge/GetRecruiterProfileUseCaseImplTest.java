package vn.talentbridge;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.port.out.CompanyRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.GetRecruiterProfileUseCaseImpl;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.vo.CompanyStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetRecruiterProfileUseCaseImplTest {

    @Mock
    private RecruiterRepositoryPort recruiterRepository;

    @Mock
    private CompanyRepositoryPort companyRepository;

    @Test
    void repairsLegacyPendingCompanyLinkBeforeReturningProfile() {
        Recruiter recruiter = new Recruiter();
        Company pending = new Company();
        pending.setId(30L);
        pending.setName("Company waiting for review");
        pending.setStatus(CompanyStatus.PENDING);
        when(recruiterRepository.findByUserId(20L)).thenReturn(Optional.of(recruiter));
        when(companyRepository.findPendingByCreatedByUserId(20L))
                .thenReturn(Optional.of(pending));
        when(recruiterRepository.save(any(Recruiter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = new GetRecruiterProfileUseCaseImpl(
                recruiterRepository,
                companyRepository
        ).getProfile(20L);

        assertThat(result.companyId()).isEqualTo(30L);
        assertThat(result.companyName()).isEqualTo("Company waiting for review");
        assertThat(result.companyStatus()).isEqualTo("PENDING");
        assertThat(recruiter.getCompany()).isSameAs(pending);
        verify(recruiterRepository).save(recruiter);
    }

    @Test
    void doesNotReplaceAnExistingCompanyLinkWithPendingRequest() {
        Company linked = new Company();
        linked.setId(11L);
        Recruiter recruiter = new Recruiter();
        recruiter.setCompany(linked);
        when(recruiterRepository.findByUserId(20L)).thenReturn(Optional.of(recruiter));

        var result = new GetRecruiterProfileUseCaseImpl(
                recruiterRepository,
                companyRepository
        ).getProfile(20L);

        assertThat(result.companyId()).isEqualTo(11L);
        verifyNoInteractions(companyRepository);
        verify(recruiterRepository, never()).save(any());
    }
}
