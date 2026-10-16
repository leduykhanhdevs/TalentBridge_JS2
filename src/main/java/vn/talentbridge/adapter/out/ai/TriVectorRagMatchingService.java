package vn.talentbridge.adapter.out.ai;

import org.springframework.stereotype.Service;
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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deterministic, explainable CV-to-job similarity adapter.
 *
 * <p>The three project-defined dimensions compare job requirements against explicit evidence,
 * career context, and conservative capability hints. This is lexical similarity, not an
 * embedding model, calibrated hiring prediction, or a method attributed to the AI-KM paper.</p>
 */
@Service
public class TriVectorRagMatchingService implements CandidateJobMatchingPort {
    private static final int MAX_CV_TEXT_LENGTH = 15_000;
    private static final double DIRECT_WEIGHT = 0.60;
    private static final double CONTEXT_WEIGHT = 0.25;
    private static final double INFERRED_WEIGHT = 0.15;

    private static final Set<String> STOP_WORDS = Set.of(
            "va", "cua", "cho", "trong", "cac", "nhung", "duoc", "voi", "co", "la", "tai",
            "ve", "de", "tu", "theo", "tren", "duoi", "khi", "neu", "da", "se", "dang",
            "mot", "hai", "ba", "bon", "chuyen", "mon", "tieu", "mo", "ta", "yeu", "cau",
            "tuyen", "dung", "cap", "bac", "chuc", "danh", "lap", "trinh", "vien",
            "and", "the", "for", "with", "in", "of", "to", "at", "by", "from"
    );

    private static final List<CapabilityRule> CAPABILITY_RULES = List.of(
            new CapabilityRule("Phát triển backend và API",
                    Set.of("spring", "springboot"), Set.of("backend", "api", "rest")),
            new CapabilityRule("Giao diện frontend theo component",
                    Set.of("react", "reactjs", "vue", "angular"), Set.of("frontend", "component", "ui")),
            new CapabilityRule("Cơ sở dữ liệu quan hệ và truy vấn",
                    Set.of("sql", "mysql", "postgresql", "oracle"), Set.of("database", "relational", "query")),
            new CapabilityRule("Đóng gói ứng dụng bằng container",
                    Set.of("docker"), Set.of("container", "deployment")),
            new CapabilityRule("Điều phối container",
                    Set.of("kubernetes", "k8s"), Set.of("orchestration", "container")),
            new CapabilityRule("Tự động hóa kiểm thử",
                    Set.of("selenium", "cypress", "playwright", "junit"), Set.of("testing", "automation")),
            new CapabilityRule("Hạ tầng đám mây",
                    Set.of("aws", "azure", "gcp"), Set.of("cloud", "infrastructure"))
    );

    private final JobRepositoryPort jobRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final CandidateSkillRepositoryPort candidateSkillRepository;

    public TriVectorRagMatchingService(
            JobRepositoryPort jobRepository,
            CandidateRepositoryPort candidateRepository,
            CandidateSkillRepositoryPort candidateSkillRepository) {
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.candidateSkillRepository = candidateSkillRepository;
    }

    @Override
    public CandidateJobMatchResult match(Long jobId, Long candidateId) {
        return matchCandidateToJob(jobId, candidateId, null);
    }

    public CandidateJobMatchResult matchCandidateToJob(Long jobId, Long candidateId, String resumeText) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc ID: " + jobId));

        Candidate candidate = null;
        String candidateName = "Ứng viên ẩn danh";
        List<CandidateSkill> candidateSkills = List.of();
        if (candidateId != null) {
            candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ứng viên ID: " + candidateId));
            if (candidate.getUser() != null && hasText(candidate.getUser().getFullName())) {
                candidateName = candidate.getUser().getFullName();
            }
            candidateSkills = candidateSkillRepository.findByCandidateId(candidateId);
        }

        return calculate(job, candidateId, candidateName, candidate, candidateSkills, resumeText);
    }

    @Override
    public CandidateJobMatchResult matchLocally(Long jobId, Long candidateId, String resumeText) {
        return matchCandidateToJob(jobId, candidateId, resumeText);
    }

    private CandidateJobMatchResult calculate(
            Job job,
            Long candidateId,
            String candidateName,
            Candidate candidate,
            List<CandidateSkill> candidateSkills,
            String resumeText) {
        Map<String, Double> requirementVector = new LinkedHashMap<>();
        addText(requirementVector, job.getTitle(), 1.5);
        addText(requirementVector, job.getExperienceLevel(), 0.75);
        addText(requirementVector, job.getRequirements(), 1.5);
        addText(requirementVector, job.getDescription(), 0.75);
        safeStrings(job.getSkills()).forEach(skill -> addText(requirementVector, skill, 2.0));

        Map<String, Double> explicitEvidenceVector = new LinkedHashMap<>();
        String candidateTitle = candidate == null ? null : candidate.getTitle();
        String candidateSummary = candidate == null ? null : candidate.getSummary();
        addText(explicitEvidenceVector, candidateTitle, 1.5);
        addText(explicitEvidenceVector, candidateSummary, 1.0);
        candidateSkills.stream()
                .map(CandidateSkill::getSkillName)
                .filter(TriVectorRagMatchingService::hasText)
                .forEach(skill -> addText(explicitEvidenceVector, skill, 2.0));
        addText(explicitEvidenceVector, truncate(resumeText), 1.0);

        Map<String, Double> jobContextVector = new LinkedHashMap<>();
        addText(jobContextVector, job.getTitle(), 1.5);
        addText(jobContextVector, job.getExperienceLevel(), 1.0);

        Map<String, Double> candidateContextVector = new LinkedHashMap<>();
        addText(candidateContextVector, candidateTitle, 1.5);
        addText(candidateContextVector, candidateSummary, 1.0);

        Set<String> explicitTerms = explicitEvidenceVector.keySet();
        Map<String, Double> inferredCapabilityVector = inferCapabilities(explicitTerms);
        double directScore = hybridSimilarity(requirementVector, explicitEvidenceVector);
        double contextualScore = hybridSimilarity(jobContextVector, candidateContextVector);
        double inferredScore = hybridSimilarity(requirementVector, inferredCapabilityVector);
        double matchPercentage = round((DIRECT_WEIGHT * directScore
                + CONTEXT_WEIGHT * contextualScore
                + INFERRED_WEIGHT * inferredScore) * 100.0, 1);

        List<String> inferredCapabilities = matchingCapabilityLabels(
                requirementVector.keySet(), explicitTerms);
        List<String> verificationQuestions = reverseSkillQuestions(job, inferredCapabilities, explicitTerms);
        List<String> strengths = matchingStrengths(job, requirementVector, explicitTerms);
        List<String> missingSkills = missingExplicitSkills(job, explicitTerms);
        String summary = "Điểm là độ tương đồng tham khảo theo ba thành phần: bằng chứng trực tiếp (60%), "
                + "ngữ cảnh nghề nghiệp (25%) và năng lực liên quan được suy luận thận trọng (15%). "
                + "Đây không phải xác suất trúng tuyển; nhà tuyển dụng cần kiểm tra CV gốc và tự quyết định.";
        String similarityBand = matchPercentage >= 70.0
                ? "ĐỘ TƯƠNG ĐỒNG THAM KHẢO CAO"
                : matchPercentage >= 40.0
                        ? "ĐỘ TƯƠNG ĐỒNG THAM KHẢO TRUNG BÌNH"
                        : "ÍT BẰNG CHỨNG TRÙNG KHỚP";

        return new CandidateJobMatchResult(
                job.getId(),
                job.getTitle(),
                candidateId,
                candidateName,
                matchPercentage,
                round(directScore, 2),
                round(contextualScore, 2),
                round(inferredScore, 2),
                inferredCapabilities,
                verificationQuestions,
                strengths,
                missingSkills,
                similarityBand,
                summary,
                AiMatchingSource.DETERMINISTIC
        );
    }

    private static double hybridSimilarity(Map<String, Double> query, Map<String, Double> evidence) {
        if (query.isEmpty() || evidence.isEmpty()) return 0.0;

        double queryMagnitude = query.values().stream().mapToDouble(Double::doubleValue).sum();
        double coveredWeight = query.entrySet().stream()
                .filter(entry -> evidence.containsKey(entry.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .sum();
        double coverage = queryMagnitude == 0.0 ? 0.0 : coveredWeight / queryMagnitude;

        double dotProduct = 0.0;
        double queryNorm = 0.0;
        double evidenceNorm = 0.0;
        for (Map.Entry<String, Double> entry : query.entrySet()) {
            double queryWeight = entry.getValue();
            double evidenceWeight = evidence.getOrDefault(entry.getKey(), 0.0);
            dotProduct += queryWeight * evidenceWeight;
            queryNorm += queryWeight * queryWeight;
        }
        for (double evidenceWeight : evidence.values()) {
            evidenceNorm += evidenceWeight * evidenceWeight;
        }
        double cosine = queryNorm == 0.0 || evidenceNorm == 0.0
                ? 0.0
                : dotProduct / (Math.sqrt(queryNorm) * Math.sqrt(evidenceNorm));
        return clamp(0.75 * coverage + 0.25 * cosine);
    }

    private static Map<String, Double> inferCapabilities(Set<String> explicitTerms) {
        Map<String, Double> inferred = new LinkedHashMap<>();
        for (CapabilityRule rule : CAPABILITY_RULES) {
            if (rule.evidenceTerms().stream().anyMatch(explicitTerms::contains)) {
                rule.capabilityTerms().forEach(term -> inferred.merge(term, 0.5, Double::sum));
            }
        }
        return inferred;
    }

    private static List<String> matchingCapabilityLabels(Set<String> requirements, Set<String> explicitTerms) {
        return CAPABILITY_RULES.stream()
                .filter(rule -> rule.evidenceTerms().stream().anyMatch(explicitTerms::contains))
                .filter(rule -> rule.capabilityTerms().stream().anyMatch(requirements::contains))
                .map(CapabilityRule::label)
                .limit(6)
                .toList();
    }

    /**
     * Reverse skill generation: turn inferred adjacent capabilities and job-skill gaps into
     * neutral interview prompts. These are verification suggestions, never assertions about a candidate.
     */
    private static List<String> reverseSkillQuestions(
            Job job, List<String> inferredCapabilities, Set<String> explicitTerms) {
        List<String> questions = new ArrayList<>();
        inferredCapabilities.stream().limit(3).forEach(capability -> questions.add(
                "Bạn có thể chia sẻ một ví dụ thực tế thể hiện năng lực “" + capability + "” không?"));
        safeStrings(job.getSkills()).stream()
                .filter(skill -> !explicitTerms.containsAll(tokens(skill)))
                .forEach(skill -> questions.add(
                        "Bạn đã từng sử dụng “" + skill + "” trong dự án hoặc công việc nào?"));
        return questions.stream().distinct().limit(5).toList();
    }

    private static List<String> matchingStrengths(
            Job job, Map<String, Double> requirements, Set<String> explicitTerms) {
        List<String> matchedSkills = safeStrings(job.getSkills()).stream()
                .filter(skill -> explicitTerms.containsAll(tokens(skill)))
                .map(skill -> "Có bằng chứng trực tiếp về: " + skill)
                .limit(5)
                .toList();
        if (!matchedSkills.isEmpty()) return matchedSkills;
        return requirements.keySet().stream()
                .filter(explicitTerms::contains)
                .sorted(Comparator.comparingDouble((String term) -> requirements.getOrDefault(term, 0.0)).reversed())
                .limit(5)
                .map(term -> "Từ khóa xuất hiện trong hồ sơ: " + term)
                .toList();
    }

    private static List<String> missingExplicitSkills(Job job, Set<String> explicitTerms) {
        return safeStrings(job.getSkills()).stream()
                .filter(skill -> !explicitTerms.containsAll(tokens(skill)))
                .map(skill -> "Chưa thấy bằng chứng trực tiếp về: " + skill)
                .limit(8)
                .toList();
    }

    private static void addText(Map<String, Double> vector, String text, double fieldWeight) {
        if (!hasText(text)) return;
        tokens(text).forEach(token -> vector.merge(token, fieldWeight, Double::sum));
    }

    private static Set<String> tokens(String text) {
        if (!hasText(text)) return Set.of();
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
        return java.util.Arrays.stream(normalized.split("[^a-z0-9+#]+"))
                .filter(token -> token.length() >= 2 && !STOP_WORDS.contains(token))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static List<String> safeStrings(List<String> values) {
        return values == null ? List.of() : values.stream().filter(TriVectorRagMatchingService::hasText).toList();
    }

    private static String truncate(String text) {
        if (text == null || text.length() <= MAX_CV_TEXT_LENGTH) return text;
        return text.substring(0, MAX_CV_TEXT_LENGTH);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static double round(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(clamp(value) * scale) / scale;
    }

    private record CapabilityRule(String label, Set<String> evidenceTerms, Set<String> capabilityTerms) {
    }
}
