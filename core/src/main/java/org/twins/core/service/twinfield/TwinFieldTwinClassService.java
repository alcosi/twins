package org.twins.core.service.twinfield;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinFieldTwinClassEntity;
import org.twins.core.dao.twin.TwinFieldTwinClassListRepository;

import java.util.UUID;

@Service
@Lazy
public class TwinFieldTwinClassService extends TwinFieldServiceBase<TwinFieldTwinClassEntity> {
    private final TwinFieldTwinClassListRepository twinFieldTwinClassListRepository;

    public TwinFieldTwinClassService(TwinFieldTwinClassListRepository twinFieldTwinClassListRepository) {
        this.twinFieldTwinClassListRepository = twinFieldTwinClassListRepository;
    }

    @Override
    public CrudRepository<TwinFieldTwinClassEntity, UUID> entityRepository() {
        return twinFieldTwinClassListRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinFieldTwinClassEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFieldTwinClassEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
