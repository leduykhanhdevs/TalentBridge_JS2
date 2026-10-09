package vn.talentbridge.adapter.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import vn.talentbridge.core.application.dto.AiMatchingSource;
import vn.talentbridge.core.application.dto.CandidateJobMatchResult;
import vn.talentbridge.core.application.port.out.CandidateJobMatchingPort;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateSkillRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.CandidateSkill;
import vn.talentbridge.core.domain.model.Job;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Legacy adapter name retained for compatibility. The deterministic fallback is a lexical heuristic,
 * not embedding-based RAG: it combines token overlap with a small rule-based capability map.
 */
@Slf4j
@Service
public class TriVectorRagMatchingService implements CandidateJobMatchingPort {

    private static final int MAX_PROMPT_SECTION_LENGTH = 15_000;

    private final JobRepositoryPort jobRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final CandidateSkillRepositoryPort candidateSkillRepository;
    private final ObjectMapper objectMapper;
    private final GeminiApiClient geminiApiClient;

    @Value("${talentbridge.gemini.model:gemini-3.5-flash-lite}")
    private String model;

    @Autowired
    public TriVectorRagMatchingService(
            JobRepositoryPort jobRepository,
            CandidateRepositoryPort candidateRepository,
            CandidateSkillRepositoryPort candidateSkillRepository,
            ObjectMapper objectMapper,
            GeminiApiClient geminiApiClient) {
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.objectMapper = objectMapper;
        this.geminiApiClient = geminiApiClient;
    }

    public TriVectorRagMatchingService(
            JobRepositoryPort jobRepository,
            CandidateRepositoryPort candidateRepository,
            CandidateSkillRepositoryPort candidateSkillRepository,
            ObjectMapper objectMapper) {
        this(jobRepository, candidateRepository, candidateSkillRepository, objectMapper,
                new GeminiApiClient("", "https://generativelanguage.googleapis.com/v1beta", 5000, 20000, null));
    }

    @Override
    public CandidateJobMatchResult match(Long jobId, Long candidateId) {
        return matchCandidateToJob(jobId, candidateId, null);
    }

    public CandidateJobMatchResult matchCandidateToJob(Long jobId, Long candidateId, String overrideCvText) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc ID: " + jobId));

        String candidateName = "Ứng viên ẩn danh";
        String candidateCvContent = overrideCvText;

        if (candidateId != null) {
            Candidate candidate = candidateRepository.findById(candidateId)
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

        String jobText = truncate(buildJobRequirementText(job));
        candidateCvContent = truncate(candidateCvContent);

        // Gemini provides a grounded advisory evaluation; deterministic fallback is clearly labeled.
        if (geminiApiClient.isConfigured()) {
            try {
                return matchWithGeminiAssessment(job, candidateId, candidateName, jobText, candidateCvContent);
            } catch (RestClientResponseException e) {
                log.warn("[TriVector RAG] Gemini từ chối yêu cầu với HTTP {}; dùng thuật toán nội bộ.", e.getStatusCode().value());
            } catch (Exception e) {
                log.warn("[TriVector RAG] Gemini matching thất bại ({}); dùng thuật toán nội bộ.", e.getClass().getSimpleName());
            }
        }

        // Rule-based fallback. It does not use learned embeddings or a vector database.
        return matchWithHeuristicFallback(job, candidateId, candidateName, jobText, candidateCvContent);
    }

    private String buildJobRequirementText(Job job) {
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
        if (job.getSkills() != null && !job.getSkills().isEmpty()) {
            sb.append("Kỹ năng: ").append(String.join(", ", job.getSkills())).append(". ");
        }
        return sb.toString();
    }

    private String buildCandidateProfileText(Candidate candidate) {
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

        var skills = candidateSkillRepository.findByCandidateId(candidate.getId());
        if (!skills.isEmpty()) {
            sb.append("Kỹ năng: ");
            for (CandidateSkill skill : skills) {
                if (skill.getSkillName() != null) {
                    sb.append(skill.getSkillName()).append(" (")
                            .append(skill.getProficiencyLevel()).append("), ");
                }
            }
        }

        return sb.toString();
    }

    private CandidateJobMatchResult matchWithGeminiAssessment(
            Job job, Long candidateId, String candidateName, String jobText, String cvText) throws Exception {

        String prompt = """
                Bạn hỗ trợ nhà tuyển dụng đối chiếu hồ sơ với yêu cầu công việc.
                Chỉ nêu kỹ năng/năng lực có bằng chứng trong hồ sơ; phân biệt rõ dữ kiện được nêu và nhận định suy luận.
                Không bịa kinh nghiệm, kỹ năng hoặc thành tích. Điểm số chỉ là gợi ý tham khảo, không phải xác suất trúng tuyển hay quyết định tuyển dụng.

                Dữ liệu đầu vào:
                [YÊU CẦU CÔNG VIỆC - q]:
                %s
                
                [HỒ SƠ ỨNG VIÊN]:
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

        String responseJson = geminiApiClient.generateContent(model, requestBody);

        JsonNode root = objectMapper.readTree(responseJson);
        String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Gemini returned no matching result");
        }
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

        return new CandidateJobMatchResult(
                job.getId(), job.getTitle(), candidateId, candidateName,
                Math.round(percentage * 10.0) / 10.0,
                Math.round(directScore * 100.0) / 100.0,
                Math.round(inferredScore * 100.0) / 100.0,
                inferredList, strengths, missing,
                data.path("recommendation").asText("CẦN ĐÁNH GIÁ THÊM"),
                data.path("analysisSummary").asText("Đánh giá khớp nối ứng viên hoàn tất bằng Gemini Tri-Vector Engine."),
                AiMatchingSource.GEMINI
        );
    }

    private CandidateJobMatchResult matchWithHeuristicFallback(
            Job job, Long candidateId, String candidateName, String jobText, String cvText) {

        Set<String> jobTokens = extractTokens(jobText);
        Set<String> cvTokens = extractTokens(cvText);

        // 1. Weighted lexical overlap between job tokens and candidate profile tokens.
        double directScore = calculateWeightedLexicalScore(jobTokens, cvTokens);

        // 2. Suy luận thông tin ẩn (Inferred Hidden Information) dựa trên miền tri thức công nghệ
        Set<String> inferredCapabilities = inferHiddenCapabilities(cvTokens);
        double inferredScore = calculateWeightedLexicalScore(jobTokens, inferredCapabilities);

        // Nâng inferred score nếu có các kỹ năng nền tảng vững
        inferredScore = Math.min(1.0, inferredScore * 1.25);

        // Blend the raw-profile and rule-inferred token scores. This is not an embedding-space vector formula.
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

        return new CandidateJobMatchResult(
                job.getId(), job.getTitle(), candidateId, candidateName,
                percentage,
                Math.round(directScore * 100.0) / 100.0,
                Math.round(inferredScore * 100.0) / 100.0,
                new ArrayList<>(inferredCapabilities), strengths, missing, recommendation,
                "Điểm tham khảo từ độ phủ từ khóa và một tập quy tắc năng lực suy luận; cần HR xác minh thủ công.",
                AiMatchingSource.DETERMINISTIC
        );
    }

    private String truncate(String text) {
        return text.length() <= MAX_PROMPT_SECTION_LENGTH
                ? text
                : text.substring(0, MAX_PROMPT_SECTION_LENGTH);
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

    private double calculateWeightedLexicalScore(Set<String> jobRequirements, Set<String> candidateTokens) {
        if (jobRequirements.isEmpty() || candidateTokens.isEmpty()) return 0.0;
        long intersection = jobRequirements.stream().filter(candidateTokens::contains).count();
        if (intersection == 0) return 0.0;

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
