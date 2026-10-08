package vn.talentbridge.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.talentbridge.core.application.dto.CandidateJobMatchResult;

import java.util.List;

/**
 * Kết quả khớp nối ứng viên - công việc áp dụng phương pháp luận từ bài báo khoa học:
 * "Version 5.4.18 - AI-KM: Knowledge enhancement with RAG and workflow" (SoftwareX 2025)
 * Thuật toán: Tri-Vector Hybrid Similarity Merge(q, v_i, u_i) = (cos(q, v_i) + cos(q, u_i)) / 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMatchResultResponse {

    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateName;

    /**
     * Điểm tương đồng tổng hợp (%): Merge(q, v_i, u_i) * 100
     */
    private Double matchPercentage;

    /**
     * Điểm tương đồng trực tiếp giữa JD và văn bản CV thô: cos(q, u_i)
     */
    private Double directKeywordScore;

    /**
     * Điểm tương đồng giữa JD và năng lực suy luận ẩn: cos(q, v_i)
     */
    private Double inferredCapabilityScore;

    /**
     * Danh sách các năng lực tiềm năng được AI suy luận ẩn (Inferred Hidden Capabilities)
     */
    private List<String> inferredCapabilities;

    /**
     * Các điểm mạnh vượt trội của ứng viên phù hợp với vị trí
     */
    private List<String> matchingStrengths;

    /**
     * Các kỹ năng hoặc yêu cầu quan trọng còn thiếu
     */
    private List<String> missingCriticalSkills;

    /**
     * Đề xuất tuyển dụng (VD: "ƯU TIÊN PHỎNG VẤN", "CẦN ĐÁNH GIÁ THÊM", "CHƯA PHÙ HỢP")
     */
    private String recommendation;

    /** Provider used for the analysis: GEMINI or DETERMINISTIC. */
    private String matchingSource;

    /**
     * Tóm tắt phân tích tổng quan
     */
    private String analysisSummary;

    public static AiMatchResultResponse from(CandidateJobMatchResult result) {
        if (result == null) return null;
        return AiMatchResultResponse.builder()
                .jobId(result.jobId())
                .jobTitle(result.jobTitle())
                .candidateId(result.candidateId())
                .candidateName(result.candidateName())
                .matchPercentage(result.matchPercentage())
                .directKeywordScore(result.directKeywordScore())
                .inferredCapabilityScore(result.inferredCapabilityScore())
                .inferredCapabilities(result.inferredCapabilities())
                .matchingStrengths(result.matchingStrengths())
                .missingCriticalSkills(result.missingCriticalSkills())
                .recommendation(result.recommendation())
                .analysisSummary(result.analysisSummary())
                .matchingSource(result.source() != null ? result.source().name() : "DETERMINISTIC")
                .build();
    }
}
