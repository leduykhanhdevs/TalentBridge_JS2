package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.talentbridge.adapter.out.persistence.entity.CvTemplateJpaEntity;

import java.util.List;
import java.util.Optional;

public interface CvTemplateJpaRepository extends JpaRepository<CvTemplateJpaEntity, Integer> {
    List<CvTemplateJpaEntity> findByIsActiveTrue();
    Optional<CvTemplateJpaEntity> findByTemplateCode(String templateCode);
}
