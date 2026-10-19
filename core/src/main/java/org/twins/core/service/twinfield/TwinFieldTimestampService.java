package org.twins.core.service.twinfield;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinFieldTimestampEntity;
import org.twins.core.dao.twin.TwinFieldTimestampRepository;

import java.util.UUID;

@Service
@Lazy
public class TwinFieldTimestampService extends TwinFieldServiceBase<TwinFieldTimestampEntity> {
    private final TwinFieldTimestampRepository twinFieldTimestampRepository;

    public TwinFieldTimestampService(TwinFieldTimestampRepository twinFieldTimestampRepository) {
        this.twinFieldTimestampRepository = twinFieldTimestampRepository;
    }

    @Override
    public CrudRepository<TwinFieldTimestampEntity, UUID> entityRepository() {
        return twinFieldTimestampRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinFieldTimestampEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFieldTimestampEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
