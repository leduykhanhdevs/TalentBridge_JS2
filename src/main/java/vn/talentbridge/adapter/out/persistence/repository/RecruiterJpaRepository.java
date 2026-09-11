package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.RecruiterJpaEntity;

import java.util.Optional;

@Repository
public interface RecruiterJpaRepository extends JpaRepository<RecruiterJpaEntity, Long> {

    @EntityGraph(attributePaths = {"user", "user.roles", "company"})
    Optional<RecruiterJpaEntity> findById(Long id);

    @EntityGraph(attributePaths = {"user", "user.roles", "company"})
    Optional<RecruiterJpaEntity> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "user.roles", "company"})
    @Query("SELECT r FROM RecruiterJpaEntity r " +
           "LEFT JOIN r.user u " +
           "LEFT JOIN r.company c " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<RecruiterJpaEntity> searchRecruiters(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(r) FROM RecruiterJpaEntity r " +
           "LEFT JOIN r.user u " +
           "LEFT JOIN r.company c " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    long countSearchRecruiters(@Param("keyword") String keyword);
}