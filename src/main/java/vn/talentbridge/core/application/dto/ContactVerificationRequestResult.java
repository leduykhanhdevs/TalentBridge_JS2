package vn.talentbridge.core.application.dto;

public record ContactVerificationRequestResult(
        String destination,
        boolean alreadyVerified,
        boolean deliveryAccepted
) {}