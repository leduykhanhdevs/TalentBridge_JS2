package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.InterviewJpaEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewJpaRepository extends JpaRepository<InterviewJpaEntity, Long> {

    @EntityGraph(attributePaths = {"application", "application.job", "application.job.company", "application.candidate", "application.candidate.user"})
    List<InterviewJpaEntity> findByApplicationIdOrderByInterviewTimeDesc(Long applicationId);

    @EntityGraph(attributePaths = {"application", "application.job", "application.job.company", "application.candidate", "application.candidate.user"})
    Optional<InterviewJpaEntity> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"application", "application.job", "application.job.company", "application.candidate", "application.candidate.user"})
    Optional<InterviewJpaEntity> findFirstByApplicationIdOrderByInterviewTimeDesc(Long applicationId);

    @Query("select i from InterviewJpaEntity i " +
           "join fetch i.application a " +
           "join fetch a.job j " +
           "join fetch a.candidate c " +
           "join fetch c.user u " +
           "where a.candidate.user.id = :candidateUserId " +
           "order by i.interviewTime desc")
    List<InterviewJpaEntity> findByCandidateUserId(@Param("candidateUserId") Long candidateUserId);
}
