package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.dao.datalist.DataListOptionProjectionRepository;

import java.util.UUID;
import java.util.function.Function;

@Service
@Lazy
@RequiredArgsConstructor
public class DataListOptionProjectionService extends EntitySecureFindServiceImpl<DataListOptionProjectionEntity> {
    private final DataListOptionProjectionRepository dataListOptionProjectionRepository;

    @Override
    public CrudRepository<DataListOptionProjectionEntity, UUID> entityRepository() {
        return dataListOptionProjectionRepository;
    }

    @Override
    public Function<DataListOptionProjectionEntity, UUID> entityGetIdFunction() {
        return DataListOptionProjectionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DataListOptionProjectionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(DataListOptionProjectionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
