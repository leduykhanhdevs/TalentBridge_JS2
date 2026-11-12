package vn.talentbridge.core.application.dto;

/** Identifies the parser or fallback reason returned for a CV import. */
public enum CvParsingSource {
    GEMINI,
    RULE_BASED,
    RULE_BASED_NO_API_KEY,
    RULE_BASED_AI_ERROR,
    RULE_BASED_TEXT_EXTRACTION
}
