package org.twins.core.service.twinfield;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinFieldSimpleNonIndexedEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNonIndexedRepository;

import java.util.UUID;

@Service
@Lazy
public class TwinFieldSimpleNonIndexedService extends TwinFieldServiceBase<TwinFieldSimpleNonIndexedEntity> {
    private final TwinFieldSimpleNonIndexedRepository twinFieldSimpleNonIndexedRepository;

    public TwinFieldSimpleNonIndexedService(TwinFieldSimpleNonIndexedRepository twinFieldSimpleNonIndexedRepository) {
        this.twinFieldSimpleNonIndexedRepository = twinFieldSimpleNonIndexedRepository;
    }

    @Override
    public CrudRepository<TwinFieldSimpleNonIndexedEntity, UUID> entityRepository() {
        return twinFieldSimpleNonIndexedRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinFieldSimpleNonIndexedEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFieldSimpleNonIndexedEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
