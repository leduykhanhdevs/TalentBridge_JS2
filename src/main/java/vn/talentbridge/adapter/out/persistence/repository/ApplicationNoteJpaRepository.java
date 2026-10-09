package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.ApplicationNoteJpaEntity;

import java.util.List;

@Repository
public interface ApplicationNoteJpaRepository extends JpaRepository<ApplicationNoteJpaEntity, Long> {

    @EntityGraph(attributePaths = {"recruiter", "recruiter.user"})
    List<ApplicationNoteJpaEntity> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);

    @Query("SELECT AVG(CAST(n.rating as double)) FROM ApplicationNoteJpaEntity n WHERE n.application.id = :applicationId AND n.rating IS NOT NULL")
    Double findAverageRatingByApplicationId(@Param("applicationId") Long applicationId);

    int countByApplicationId(Long applicationId);

    @Query("SELECT n.application.id, AVG(CAST(n.rating as double)), COUNT(n.id) " +
            "FROM ApplicationNoteJpaEntity n " +
            "WHERE n.application.id IN :applicationIds " +
            "GROUP BY n.application.id")
    List<Object[]> findRatingSummariesByApplicationIds(@Param("applicationIds") List<Long> applicationIds);
}
