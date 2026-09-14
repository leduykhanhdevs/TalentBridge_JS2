package vn.talentbridge.core.application.dto;

public record SubmitJoinCompanyRequestCommand(
        String position,
        String message
) {
}
