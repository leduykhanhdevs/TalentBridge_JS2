package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.adapter.out.ai.TriVectorRagMatchingService;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateSkillRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.dto.CandidateJobMatchResult;
import vn.talentbridge.core.domain.model.Job;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriVectorRagMatchingServiceTest {

    @Mock
    private JobRepositoryPort jobRepository;

    @Mock
    private CandidateRepositoryPort candidateRepository;

    @Mock
    private CandidateSkillRepositoryPort candidateSkillRepository;

    private TriVectorRagMatchingService matchingService;

    @BeforeEach
    void setUp() {
        matchingService = new TriVectorRagMatchingService(
                jobRepository,
                candidateRepository,
                candidateSkillRepository,
                new ObjectMapper()
        );
    }

    @Test
    @DisplayName("Deterministic matching uses token coverage and rule-based capability inference")
    void shouldCalculateLexicalMatchAndInferredCapabilities() {
        // Arrange (AAA Pattern)
        Long jobId = 1L;
        Job mockJob = new Job();
        mockJob.setId(jobId);
        mockJob.setTitle("Senior Java Spring Boot Developer");
        mockJob.setDescription("Phát triển backend hiệu năng cao, thiết kế REST API, kiến trúc microservices");
        mockJob.setRequirements("Thành thạo Java 21, Spring Boot 3, MySQL, Docker, CI/CD pipeline");
        mockJob.setExperienceLevel("SENIOR");

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(mockJob));

        String candidateCv = """
                Lập trình viên Backend với 4 năm kinh nghiệm.
                Chuyên môn: Java, Spring Boot, MySQL, RESTful API.
                Đã xây dựng hệ thống thanh toán và triển khai Docker container.
                """;

        // Act
        CandidateJobMatchResult result = matchingService.matchCandidateToJob(jobId, null, candidateCv);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.jobId()).isEqualTo(jobId);
        assertThat(result.directKeywordScore()).isGreaterThan(0.0);
        assertThat(result.inferredCapabilityScore()).isGreaterThan(0.0);
        assertThat(result.matchPercentage()).isGreaterThan(40.0);
        assertThat(result.inferredCapabilities()).isNotEmpty();
        assertThat(result.inferredCapabilities()).contains("microservices", "backend");
        assertThat(result.recommendation()).isIn("ƯU TIÊN PHỎNG VẤN", "CẦN ĐÁNH GIÁ THÊM");
        assertThat(result.source().name()).isEqualTo("DETERMINISTIC");
    }

    @Test
    @DisplayName("Unrelated candidate profile receives a low lexical match score")
    void shouldIdentifyUnsuitableCandidateCorrectly() {
        // Arrange
        Long jobId = 2L;
        Job mockJob = new Job();
        mockJob.setId(jobId);
        mockJob.setTitle("Chuyên viên Marketing & SEO");
        mockJob.setDescription("Chạy quảng cáo Facebook Ads, Google Ads, sáng tạo nội dung truyền thông");
        mockJob.setRequirements("Kinh nghiệm copywriting, SEO top 1 Google, Photoshop");

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(mockJob));

        String technicalCv = "Lập trình viên vi điều khiển C/C++, hàn mạch điện tử.";

        // Act
        CandidateJobMatchResult result = matchingService.matchCandidateToJob(jobId, null, technicalCv);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.matchPercentage()).isLessThan(50.0);
        assertThat(result.recommendation()).isEqualTo("CHƯA PHÙ HỢP");
    }

    @Test
    @DisplayName("Empty candidate evidence must not receive a fabricated match baseline")
    void shouldReturnZeroWhenCandidateHasNoMatchingEvidence() {
        Long jobId = 3L;
        Job job = new Job();
        job.setId(jobId);
        job.setTitle("Senior Java Developer");
        job.setRequirements("Java Spring Boot SQL");
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        CandidateJobMatchResult result = matchingService.matchCandidateToJob(jobId, null, "");

        assertThat(result.matchPercentage()).isZero();
        assertThat(result.recommendation()).isEqualTo("CHƯA PHÙ HỢP");
    }
}
