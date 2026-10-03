package vn.talentbridge.config;

import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;
import vn.talentbridge.core.application.dto.ReviewJoinRequestCommand;
import vn.talentbridge.core.application.port.in.ReviewJoinRequestUseCase;

public class TransactionalReviewJoinRequestUseCase implements ReviewJoinRequestUseCase {
    private final ReviewJoinRequestUseCase delegate;

    public TransactionalReviewJoinRequestUseCase(ReviewJoinRequestUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public CompanyJoinRequestResult reviewJoinRequest(Long reviewerUserId, Long requestId, ReviewJoinRequestCommand command) {
        return delegate.reviewJoinRequest(reviewerUserId, requestId, command);
    }
}
