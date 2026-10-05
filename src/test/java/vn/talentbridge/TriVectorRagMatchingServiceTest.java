package vn.talentbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.adapter.in.web.dto.response.AiMatchResultResponse;
import vn.talentbridge.adapter.out.ai.TriVectorRagMatchingService;
import vn.talentbridge.adapter.out.persistence.entity.JobJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.CandidateSkillJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.JobJpaRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriVectorRagMatchingServiceTest {

    @Mock
    private JobJpaRepository jobRepository;

    @Mock
    private CandidateJpaRepository candidateRepository;

    @Mock
    private CandidateSkillJpaRepository candidateSkillRepository;

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
    @DisplayName("Nghiệm thu thuật toán Tri-Vector RAG từ bài báo SoftwareX 2025: Khớp nối CV và JD")
    void shouldCalculateTriVectorHybridSimilarityCorrectly() {
        // Arrange (AAA Pattern)
        Long jobId = 1L;
        JobJpaEntity mockJob = JobJpaEntity.builder()
                .id(jobId)
                .title("Senior Java Spring Boot Developer")
                .description("Phát triển backend hiệu năng cao, thiết kế REST API, kiến trúc microservices")
                .requirements("Thành thạo Java 21, Spring Boot 3, MySQL, Docker, CI/CD pipeline")
                .experienceLevel("SENIOR")
                .build();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(mockJob));

        String candidateCv = """
                Lập trình viên Backend với 4 năm kinh nghiệm.
                Chuyên môn: Java, Spring Boot, MySQL, RESTful API.
                Đã xây dựng hệ thống thanh toán và triển khai Docker container.
                """;

        // Act
        AiMatchResultResponse result = matchingService.matchCandidateToJob(jobId, null, candidateCv);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getJobId()).isEqualTo(jobId);
        assertThat(result.getDirectKeywordScore()).isGreaterThan(0.0);
        assertThat(result.getInferredCapabilityScore()).isGreaterThan(0.0);
        assertThat(result.getMatchPercentage()).isGreaterThan(40.0);
        assertThat(result.getInferredCapabilities()).isNotEmpty();
        assertThat(result.getInferredCapabilities()).contains("microservices", "backend");
        assertThat(result.getRecommendation()).isIn("ƯU TIÊN PHỎNG VẤN", "CẦN ĐÁNH GIÁ THÊM");
    }

    @Test
    @DisplayName("Đánh giá ứng viên không phù hợp: Điểm tương đồng Tri-Vector thấp")
    void shouldIdentifyUnsuitableCandidateCorrectly() {
        // Arrange
        Long jobId = 2L;
        JobJpaEntity mockJob = JobJpaEntity.builder()
                .id(jobId)
                .title("Chuyên viên Marketing & SEO")
                .description("Chạy quảng cáo Facebook Ads, Google Ads, sáng tạo nội dung truyền thông")
                .requirements("Kinh nghiệm copywriting, SEO top 1 Google, Photoshop")
                .build();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(mockJob));

        String technicalCv = "Lập trình viên vi điều khiển C/C++, hàn mạch điện tử.";

        // Act
        AiMatchResultResponse result = matchingService.matchCandidateToJob(jobId, null, technicalCv);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMatchPercentage()).isLessThan(50.0);
        assertThat(result.getRecommendation()).isEqualTo("CHƯA PHÙ HỢP");
    }
}
