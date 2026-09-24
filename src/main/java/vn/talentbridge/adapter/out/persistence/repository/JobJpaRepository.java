package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.JobJpaEntity;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.util.Optional;

@Repository
public interface JobJpaRepository extends JpaRepository<JobJpaEntity, Long>, JpaSpecificationExecutor<JobJpaEntity> {

    @EntityGraph(attributePaths = {"company", "skills"})
    Optional<JobJpaEntity> findById(Long id);

    @EntityGraph(attributePaths = {"company", "skills"})
    Page<JobJpaEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"company", "skills"})
    Page<JobJpaEntity> findByStatus(JobStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"company", "skills"})
    Page<JobJpaEntity> findByRecruiterUserId(Long recruiterUserId, Pageable pageable);

    @EntityGraph(attributePaths = {"company", "skills"})
    Page<JobJpaEntity> findByRecruiterUserIdAndStatus(Long recruiterUserId, JobStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"company", "skills"})
    Page<JobJpaEntity> findByCompanyId(Long companyId, Pageable pageable);

    @EntityGraph(attributePaths = {"company", "skills"})
    Page<JobJpaEntity> findByCompanyIdAndStatus(Long companyId, JobStatus status, Pageable pageable);

    long countByStatus(JobStatus status);
}