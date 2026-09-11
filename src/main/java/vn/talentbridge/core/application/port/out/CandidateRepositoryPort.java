package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.List;
import java.util.Optional;

public interface CandidateRepositoryPort {
    Optional<Candidate> findById(Long id);
    Optional<Candidate> findByUserId(Long userId);
    List<Candidate> findAll(int page, int size, String keyword, UserStatus status);
    long count(String keyword, UserStatus status);
    Candidate save(Candidate candidate);
}
