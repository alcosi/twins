package org.twins.core.service.twinfield;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinFieldI18nEntity;
import org.twins.core.dao.twin.TwinFieldI18nRepository;

import java.util.UUID;

@Service
@Lazy
public class TwinFieldI18nService extends TwinFieldServiceBase<TwinFieldI18nEntity> {
    private final TwinFieldI18nRepository twinFieldI18nRepository;

    public TwinFieldI18nService(TwinFieldI18nRepository twinFieldI18nRepository) {
        this.twinFieldI18nRepository = twinFieldI18nRepository;
    }

    @Override
    public CrudRepository<TwinFieldI18nEntity, UUID> entityRepository() {
        return twinFieldI18nRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinFieldI18nEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFieldI18nEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
