package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.Application;

public interface ApplicationRepositoryPort {
    Application save(Application application);
}