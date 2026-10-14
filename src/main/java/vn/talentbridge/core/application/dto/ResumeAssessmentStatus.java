package vn.talentbridge.core.application.dto;

/** Describes whether stored CV content was available to the local assessment pipeline. */
public enum ResumeAssessmentStatus {
    EVALUATED,
    NO_RESUME,
    CONTENT_UNAVAILABLE,
    NO_READABLE_TEXT
}
