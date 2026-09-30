package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.ApplicationNote;

import java.util.List;

public interface ApplicationNoteRepositoryPort {

    ApplicationNote save(ApplicationNote note);

    List<ApplicationNote> findByApplicationId(Long applicationId);

    Double getAverageRatingByApplicationId(Long applicationId);

    int countByApplicationId(Long applicationId);
}
