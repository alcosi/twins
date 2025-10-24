package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListProjectionEntity;
import org.twins.core.dao.datalist.DataListProjectionRepository;

import java.util.UUID;
import java.util.function.Function;

@Service
@Lazy
@RequiredArgsConstructor
public class DataListProjectionService extends EntitySecureFindServiceImpl<DataListProjectionEntity> {
    private final DataListProjectionRepository dataListProjectionRepository;

    @Override
    public CrudRepository<DataListProjectionEntity, UUID> entityRepository() {
        return dataListProjectionRepository;
    }

    @Override
    public Function<DataListProjectionEntity, UUID> entityGetIdFunction() {
        return DataListProjectionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DataListProjectionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(DataListProjectionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
