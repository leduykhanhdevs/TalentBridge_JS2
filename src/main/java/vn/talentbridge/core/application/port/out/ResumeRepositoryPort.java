package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.Resume;

import java.util.List;
import java.util.Optional;

public interface ResumeRepositoryPort {

    Resume save(Resume resume);

    Optional<Resume> findById(Long id);

    List<Resume> findByCandidateId(Long candidateId);

    void delete(Long id);

    void clearDefault(Long candidateId);

    Optional<Resume> findDefaultByCandidateId(Long candidateId);
}
