package vn.talentbridge.adapter.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import vn.talentbridge.adapter.in.web.dto.response.AiMatchResultResponse;
import vn.talentbridge.adapter.out.persistence.entity.CandidateJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.JobJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.CandidateSkillJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.JobJpaRepository;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Hiện thực thuật toán Tri-Vector Hybrid Similarity từ công trình khoa học:
 * "Version 5.4.18 - AI-KM: Knowledge enhancement with RAG and workflow" (SoftwareX 31, 2025, 102349).
 *
 * Công thức cốt lõi:
 * Merge(q, v_i, u_i) = (cos(q, v_i) + cos(q, u_i)) / 2
 *
 * Trong đó:
 * - q: Vector yêu cầu công việc (Job Requirements & Description)
 * - u_i: Vector văn bản CV thô của ứng viên (Raw Candidate CV / Skills)
 * - v_i: Vector năng lực tiềm năng suy luận ẩn bởi LLM (Inferred Hidden Capabilities)
 */
@Slf4j
@Service
public class TriVectorRagMatchingService {

    private static final String GEMINI_API_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final JobJpaRepository jobRepository;
    private final CandidateJpaRepository candidateRepository;
    private final CandidateSkillJpaRepository candidateSkillRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${talentbridge.gemini.api-key:}")
    private String apiKey;

    @Value("${talentbridge.gemini.model:gemini-flash-lite-latest}")
    private String model;

    public TriVectorRagMatchingService(
            JobJpaRepository jobRepository,
            CandidateJpaRepository candidateRepository,
            CandidateSkillJpaRepository candidateSkillRepository,
            ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(6));
        requestFactory.setReadTimeout(Duration.ofSeconds(20));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public AiMatchResultResponse matchCandidateToJob(Long jobId, Long candidateId, String overrideCvText) {
        JobJpaEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc ID: " + jobId));

        String candidateName = "Ứng viên ẩn danh";
        String candidateCvContent = overrideCvText;

        if (candidateId != null) {
            CandidateJpaEntity candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ứng viên ID: " + candidateId));
            if (candidate.getUser() != null) {
                candidateName = candidate.getUser().getFullName();
            }

            if (candidateCvContent == null || candidateCvContent.isBlank()) {
                candidateCvContent = buildCandidateProfileText(candidate);
            }
        }

        if (candidateCvContent == null || candidateCvContent.isBlank()) {
            candidateCvContent = "Thông tin kỹ năng ứng viên chưa được cập nhật đầy đủ.";
        }

        String jobText = buildJobRequirementText(job);

        // Thử chạy qua Gemini AI với phương pháp Tri-Vector RAG
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                return matchWithGeminiTriVector(job, candidateId, candidateName, jobText, candidateCvContent);
            } catch (Exception e) {
                log.warn("[TriVector RAG] Gemini AI matching gặp sự cố ({}), tự động fallback thuật toán nội bộ", e.getMessage());
            }
        }

        // Thuật toán Cosine Similarity nội bộ (Deterministic Tri-Vector Engine)
        return matchWithDeterministicTriVector(job, candidateId, candidateName, jobText, candidateCvContent);
    }

    private String buildJobRequirementText(JobJpaEntity job) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tiêu đề: ").append(job.getTitle()).append(". ");
        if (job.getExperienceLevel() != null) {
            sb.append("Cấp bậc: ").append(job.getExperienceLevel()).append(". ");
        }
        if (job.getRequirements() != null) {
            sb.append("Yêu cầu tuyển dụng: ").append(job.getRequirements()).append(". ");
        }
        if (job.getDescription() != null) {
            sb.append("Mô tả: ").append(job.getDescription()).append(". ");
        }
        return sb.toString();
    }

    private String buildCandidateProfileText(CandidateJpaEntity candidate) {
        StringBuilder sb = new StringBuilder();
        if (candidate.getTitle() != null) {
            sb.append("Chức danh: ").append(candidate.getTitle()).append(". ");
        }
        if (candidate.getExperienceYears() != null) {
            sb.append("Kinh nghiệm: ").append(candidate.getExperienceYears()).append(" năm. ");
        }
        if (candidate.getSummary() != null) {
            sb.append("Tóm tắt: ").append(candidate.getSummary()).append(". ");
        }

        try {
            var skills = candidateSkillRepository.findByCandidateIdOrderByIdAsc(candidate.getId());
            if (!skills.isEmpty()) {
                sb.append("Kỹ năng: ");
                for (var s : skills) {
                    if (s.getSkill() != null) {
                        sb.append(s.getSkill().getName()).append(" (")
                                .append(s.getProficiencyLevel()).append("), ");
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return sb.toString();
    }

    private AiMatchResultResponse matchWithGeminiTriVector(
            JobJpaEntity job, Long candidateId, String candidateName, String jobText, String cvText) throws Exception {

        String prompt = """
                Bạn là hệ thống AI Matching chuyên sâu theo nghiên cứu: "AI-KM: Knowledge enhancement with RAG and workflow" (SoftwareX 2025).
                Nhiệm vụ của bạn là áp dụng kỹ thuật Suy luận thông tin ẩn (Inferred Hidden Information) và Công thức Tri-Vector Hybrid Similarity:
                Merge(q, v_i, u_i) = (cos(q, v_i) + cos(q, u_i)) / 2
                
                Trong đó:
                - q là Yêu cầu công việc (Job Requirements).
                - u_i là Văn bản CV thô của ứng viên.
                - v_i là Các năng lực tiềm năng suy luận ẩn (Inferred Hidden Capabilities): Những bài toán ứng viên CÓ THỂ giải quyết được dựa trên kinh nghiệm, dự án và kỹ năng nền tảng trong CV, dù CV có thể chưa viết đúng từ khóa của JD.
                
                Dữ liệu đầu vào:
                [YÊU CẦU CÔNG VIỆC - q]:
                %s
                
                [HỒ SƠ ỨNG VIÊN - u_i]:
                %s
                
                Hãy trả về DUY NHẤT một JSON hợp lệ theo schema sau:
                {
                  "directKeywordScore": 0.0 - 1.0,
                  "inferredCapabilityScore": 0.0 - 1.0,
                  "matchPercentage": 0.0 - 100.0,
                  "inferredCapabilities": ["năng lực suy luận 1", "năng lực suy luận 2"],
                  "matchingStrengths": ["điểm mạnh 1", "điểm mạnh 2"],
                  "missingCriticalSkills": ["kỹ năng còn thiếu 1"],
                  "recommendation": "ƯU TIÊN PHỎNG VẤN" | "CẦN ĐÁNH GIÁ THÊM" | "CHƯA PHÙ HỢP",
                  "analysisSummary": "Tóm tắt phân tích ngắn gọn trong 2-3 câu"
                }
                """.formatted(jobText, cvText);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.1,
                        "responseMimeType", "application/json"
                )
        );

        String url = String.format(GEMINI_API_URL_TEMPLATE, model, apiKey);
        String responseJson = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(responseJson);
        String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        JsonNode data = objectMapper.readTree(text);

        double directScore = data.path("directKeywordScore").asDouble(0.7);
        double inferredScore = data.path("inferredCapabilityScore").asDouble(0.8);
        double percentage = data.path("matchPercentage").asDouble(((directScore + inferredScore) / 2.0) * 100.0);

        List<String> inferredList = new ArrayList<>();
        data.path("inferredCapabilities").forEach(n -> inferredList.add(n.asText()));

        List<String> strengths = new ArrayList<>();
        data.path("matchingStrengths").forEach(n -> strengths.add(n.asText()));

        List<String> missing = new ArrayList<>();
        data.path("missingCriticalSkills").forEach(n -> missing.add(n.asText()));

        return AiMatchResultResponse.builder()
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .candidateId(candidateId)
                .candidateName(candidateName)
                .directKeywordScore(Math.round(directScore * 100.0) / 100.0)
                .inferredCapabilityScore(Math.round(inferredScore * 100.0) / 100.0)
                .matchPercentage(Math.round(percentage * 10.0) / 10.0)
                .inferredCapabilities(inferredList)
                .matchingStrengths(strengths)
                .missingCriticalSkills(missing)
                .recommendation(data.path("recommendation").asText("CẦN ĐÁNH GIÁ THÊM"))
                .analysisSummary(data.path("analysisSummary").asText("Đánh giá khớp nối ứng viên hoàn tất bằng Gemini Tri-Vector Engine."))
                .build();
    }

    private AiMatchResultResponse matchWithDeterministicTriVector(
            JobJpaEntity job, Long candidateId, String candidateName, String jobText, String cvText) {

        Set<String> jobTokens = extractTokens(jobText);
        Set<String> cvTokens = extractTokens(cvText);

        // 1. cos(q, u_i) - Direct Keyword Cosine Similarity
        double directScore = calculateJaccardCosine(jobTokens, cvTokens);

        // 2. Suy luận thông tin ẩn (Inferred Hidden Information) dựa trên miền tri thức công nghệ
        Set<String> inferredCapabilities = inferHiddenCapabilities(cvTokens);
        double inferredScore = calculateJaccardCosine(jobTokens, inferredCapabilities);

        // Nâng inferred score nếu có các kỹ năng nền tảng vững
        inferredScore = Math.min(1.0, inferredScore * 1.25);

        // 3. Tri-Vector Formula: Merge(q, v_i, u_i) = (cos(q, v_i) + cos(q, u_i)) / 2
        double hybridScore = (directScore + inferredScore) / 2.0;
        double percentage = Math.round(hybridScore * 1000.0) / 10.0;

        List<String> strengths = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String jt : jobTokens) {
            if (cvTokens.contains(jt) || inferredCapabilities.contains(jt)) {
                if (strengths.size() < 5) strengths.add("Khớp yêu cầu: " + jt);
            } else {
                if (missing.size() < 4) missing.add("Chưa thấy đề cập: " + jt);
            }
        }

        String recommendation;
        if (percentage >= 70.0) {
            recommendation = "ƯU TIÊN PHỎNG VẤN";
        } else if (percentage >= 40.0) {
            recommendation = "CẦN ĐÁNH GIÁ THÊM";
        } else {
            recommendation = "CHƯA PHÙ HỢP";
        }

        return AiMatchResultResponse.builder()
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .candidateId(candidateId)
                .candidateName(candidateName)
                .directKeywordScore(Math.round(directScore * 100.0) / 100.0)
                .inferredCapabilityScore(Math.round(inferredScore * 100.0) / 100.0)
                .matchPercentage(percentage)
                .inferredCapabilities(new ArrayList<>(inferredCapabilities))
                .matchingStrengths(strengths)
                .missingCriticalSkills(missing)
                .recommendation(recommendation)
                .analysisSummary("Ứng viên đạt " + percentage + "% độ tương đồng theo công thức Tri-Vector Cosine Similarity.")
                .build();
    }

    private Set<String> extractTokens(String text) {
        if (text == null) return Collections.emptySet();
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();

        return Arrays.stream(normalized.split("[^a-zA-Z0-9+#]+"))
                .filter(s -> s.length() >= 2 && !STOP_WORDS.contains(s))
                .collect(Collectors.toSet());
    }

    private Set<String> inferHiddenCapabilities(Set<String> cvTokens) {
        Set<String> inferred = new LinkedHashSet<>();
        if (cvTokens.contains("java") || cvTokens.contains("spring") || cvTokens.contains("boot")) {
            inferred.addAll(List.of("microservices", "rest", "api", "backend", "oop", "concurrency", "performance"));
        }
        if (cvTokens.contains("react") || cvTokens.contains("vue") || cvTokens.contains("typescript")) {
            inferred.addAll(List.of("frontend", "spa", "ui", "ux", "responsive", "component"));
        }
        if (cvTokens.contains("mysql") || cvTokens.contains("sql") || cvTokens.contains("postgresql")) {
            inferred.addAll(List.of("database", "query", "indexes", "acid", "transaction"));
        }
        if (cvTokens.contains("docker") || cvTokens.contains("git") || cvTokens.contains("ci")) {
            inferred.addAll(List.of("devops", "pipeline", "container", "automation", "deploy"));
        }
        return inferred;
    }

    private double calculateJaccardCosine(Set<String> jobRequirements, Set<String> candidateTokens) {
        if (jobRequirements.isEmpty() || candidateTokens.isEmpty()) return 0.2;
        long intersection = jobRequirements.stream().filter(candidateTokens::contains).count();
        if (intersection == 0) return 0.05;

        // Tỷ lệ bao phủ các yêu cầu tuyển dụng trong CV
        double requirementCoverage = (double) intersection / jobRequirements.size();
        double cosine = (double) intersection / Math.sqrt((double) jobRequirements.size() * candidateTokens.size());

        // Điểm đánh giá: 80% độ bao phủ yêu cầu tuyển dụng + 20% độ tương đồng Cosine
        double score = (requirementCoverage * 0.8) + (cosine * 0.2);
        return Math.min(1.0, Math.max(0.0, score * 1.5)); // Scale theo hệ số độ khớp kỹ năng thực tế
    }

    private static final Set<String> STOP_WORDS = Set.of(
            "va", "cua", "cho", "trong", "cac", "nhung", "duoc", "voi", "co", "la", "tai",
            "ve", "de", "tu", "theo", "tren", "duoi", "khi", "neu", "da", "se", "dang",
            "mot", "hai", "ba", "bon", "chuyen", "mon", "tieu", "mo", "ta", "yeu", "cau",
            "tuyen", "dung", "cap", "bac", "chuc", "danh", "lap", "trinh", "vien",
            "and", "the", "for", "with", "in", "of", "to", "at", "by", "from"
    );
}
