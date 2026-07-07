package org.twins.core.service.twinfield;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinFieldUserEntity;
import org.twins.core.dao.twin.TwinFieldUserRepository;

import java.util.UUID;

@Service
@Lazy
public class TwinFieldUserService extends TwinFieldServiceBase<TwinFieldUserEntity> {
    private final TwinFieldUserRepository twinFieldUserRepository;

    public TwinFieldUserService(TwinFieldUserRepository twinFieldUserRepository) {
        this.twinFieldUserRepository = twinFieldUserRepository;
    }

    @Override
    public CrudRepository<TwinFieldUserEntity, UUID> entityRepository() {
        return twinFieldUserRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinFieldUserEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFieldUserEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
