package vn.talentbridge;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.JobJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.JobJpaRepository;
import vn.talentbridge.core.application.port.in.JobModerationUseCase;
import vn.talentbridge.core.application.port.out.JobStatusHistoryRepositoryPort;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class JobModerationRollbackIntegrationTest {
    @Autowired private JobModerationUseCase jobModerationUseCase;
    @Autowired private CompanyJpaRepository companyRepository;
    @Autowired private JobJpaRepository jobRepository;
    @Autowired private EntityManager entityManager;
    @MockBean private JobStatusHistoryRepositoryPort historyRepository;

    private Long jobId;
    private Long companyId;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        CompanyJpaEntity company = companyRepository.save(CompanyJpaEntity.builder()
                .name("Moderation rollback test")
                .taxCode("rollback-" + System.nanoTime())
                .status(CompanyStatus.APPROVED)
                .build());
        companyId = company.getId();
        JobJpaEntity job = jobRepository.save(JobJpaEntity.builder()
                .company(company)
                .title("Rollback moderation state")
                .description("Test transaction boundary")
                .status(JobStatus.PENDING)
                .deadline(LocalDate.now().plusDays(10))
                .build());
        jobId = job.getId();
    }

    @AfterEach
    void cleanUp() {
        if (jobId != null) jobRepository.deleteById(jobId);
        if (companyId != null) companyRepository.deleteById(companyId);
    }

    @Test
    void rollsBackJobStateWhenModerationHistoryCannotBeSaved() {
        doThrow(new IllegalStateException("history store unavailable"))
                .when(historyRepository).save(any());

        assertThatThrownBy(() -> jobModerationUseCase.changeStatus(1L, jobId, JobStatus.ACTIVE, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("history store unavailable");

        entityManager.clear();
        assertThat(jobRepository.findById(jobId).orElseThrow().getStatus()).isEqualTo(JobStatus.PENDING);
    }
}
