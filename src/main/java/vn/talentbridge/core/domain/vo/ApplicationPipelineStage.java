package vn.talentbridge.core.domain.vo;

import java.util.Locale;

public enum ApplicationPipelineStage {
    APPLIED(0, "SUBMITTED"),
    REVIEWING(1, "ACTIVE"),
    SHORTLISTED(2, "ACTIVE"),
    INTERVIEW(3, "ACTIVE"),
    OFFERED(4, "ACTIVE"),
    HIRED(5, "ACCEPTED"),
    REJECTED(6, "REJECTED");

    private final int order;
    private final String applicationStatus;

    ApplicationPipelineStage(int order, String applicationStatus) {
        this.order = order;
        this.applicationStatus = applicationStatus;
    }

    public String applicationStatus() {
        return applicationStatus;
    }

    public boolean canAdvanceTo(ApplicationPipelineStage target) {
        if (this == HIRED || this == REJECTED || target == this || target == APPLIED) return false;
        return target == REJECTED || target.order > order;
    }

    public static ApplicationPipelineStage from(String value) {
        if (value == null || value.isBlank()) return APPLIED;
        if ("SCREENING".equalsIgnoreCase(value.trim())) return REVIEWING;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Giai đoạn tuyển dụng không hợp lệ: " + value, exception);
        }
    }
}
