package org.twins.core.service.twinfield;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twin.TwinFieldBooleanRepository;

import java.util.UUID;

@Service
@Lazy
public class TwinFieldBooleanService extends TwinFieldServiceBase<TwinFieldBooleanEntity> {
    private final TwinFieldBooleanRepository twinFieldBooleanRepository;

    public TwinFieldBooleanService(TwinFieldBooleanRepository twinFieldBooleanRepository) {
        this.twinFieldBooleanRepository = twinFieldBooleanRepository;
    }

    @Override
    public CrudRepository<TwinFieldBooleanEntity, UUID> entityRepository() {
        return twinFieldBooleanRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinFieldBooleanEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFieldBooleanEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
