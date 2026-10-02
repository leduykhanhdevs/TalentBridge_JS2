package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.CvTemplate;

import java.util.List;
import java.util.Optional;

public interface CvTemplateRepositoryPort {
    List<CvTemplate> findAllActive();
    Optional<CvTemplate> findByTemplateCode(String templateCode);
}
