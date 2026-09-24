package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    User save(User user);
    List<User> findAll(int page, int size);
    List<User> findAll(int page, int size, UserStatus status);
    long count();
    long count(UserStatus status);
}