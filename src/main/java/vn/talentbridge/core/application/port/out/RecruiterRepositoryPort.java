package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.Recruiter;

import java.util.List;
import java.util.Optional;

public interface RecruiterRepositoryPort {
    Optional<Recruiter> findById(Long id);
    Optional<Recruiter> findByUserId(Long userId);
    List<Recruiter> findAll(int page, int size, String keyword);
    long count(String keyword);
    Recruiter save(Recruiter recruiter);
}