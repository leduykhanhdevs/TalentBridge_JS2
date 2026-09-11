package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.CandidateJpaEntity;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.Optional;

@Repository
public interface CandidateJpaRepository extends JpaRepository<CandidateJpaEntity, Long> {

    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<CandidateJpaEntity> findById(Long id);

    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<CandidateJpaEntity> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "user.roles"})
    @Query("SELECT c FROM CandidateJpaEntity c " +
           "LEFT JOIN c.user u " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.city) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR u.status = :status)")
    Page<CandidateJpaEntity> searchCandidates(@Param("keyword") String keyword,
                                              @Param("status") UserStatus status,
                                              Pageable pageable);

    @Query("SELECT COUNT(c) FROM CandidateJpaEntity c " +
           "LEFT JOIN c.user u " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.city) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR u.status = :status)")
    long countSearchCandidates(@Param("keyword") String keyword,
                               @Param("status") UserStatus status);
}
