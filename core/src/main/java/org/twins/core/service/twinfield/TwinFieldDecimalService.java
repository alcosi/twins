package org.twins.core.service.twinfield;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;

import java.util.UUID;

@Service
@Lazy
public class TwinFieldDecimalService extends TwinFieldServiceBase<TwinFieldDecimalEntity> {
    private final TwinFieldDecimalRepository twinFieldDecimalRepository;

    public TwinFieldDecimalService(TwinFieldDecimalRepository twinFieldDecimalRepository) {
        this.twinFieldDecimalRepository = twinFieldDecimalRepository;
    }

    @Override
    public CrudRepository<TwinFieldDecimalEntity, UUID> entityRepository() {
        return twinFieldDecimalRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinFieldDecimalEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFieldDecimalEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
