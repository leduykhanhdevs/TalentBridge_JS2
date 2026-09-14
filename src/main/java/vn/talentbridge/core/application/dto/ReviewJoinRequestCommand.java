package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;

public record ReviewJoinRequestCommand(
        CompanyJoinRequestStatus status,
        String reason
) {
}
