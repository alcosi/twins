package org.twins.core.service.twinfield;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twin.TwinFieldDataListRepository;

import java.util.UUID;

@Service
@Lazy
public class TwinFieldDataListService extends TwinFieldServiceBase<TwinFieldDataListEntity> {
    private final TwinFieldDataListRepository twinFieldDataListRepository;

    public TwinFieldDataListService(TwinFieldDataListRepository twinFieldDataListRepository) {
        this.twinFieldDataListRepository = twinFieldDataListRepository;
    }

    @Override
    public CrudRepository<TwinFieldDataListEntity, UUID> entityRepository() {
        return twinFieldDataListRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinFieldDataListEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFieldDataListEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
