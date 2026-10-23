package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.ContactVerificationStatusResult;

public interface ContactVerificationUseCase {

    enum Channel {
        EMAIL,
        PHONE
    }

    ContactVerificationStatusResult getStatus(Long userId);

    void requestCode(Long userId, Channel channel);

    ContactVerificationStatusResult verifyCode(Long userId, Channel channel, String otp);
}
