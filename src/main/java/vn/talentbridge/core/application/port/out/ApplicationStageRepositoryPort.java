package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.ApplicationStage;

import java.util.List;

public interface ApplicationStageRepositoryPort {

    ApplicationStage save(ApplicationStage stage);

    List<ApplicationStage> findByApplicationId(Long applicationId);
}
