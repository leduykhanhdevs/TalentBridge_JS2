package vn.talentbridge.adapter.out.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.talentbridge.core.application.port.out.PasswordEncoderPort;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {
    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordEncoderAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}