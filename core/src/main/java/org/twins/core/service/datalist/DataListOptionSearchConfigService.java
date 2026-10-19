package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListOptionSearchEntity;
import org.twins.core.dao.datalist.DataListOptionSearchRepository;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class DataListOptionSearchConfigService extends EntitySecureFindServiceImpl<DataListOptionSearchEntity> {
    private final AuthService authService;
    private final DataListOptionSearchRepository dataListOptionSearchRepository;

    @Override
    public CrudRepository<DataListOptionSearchEntity, UUID> entityRepository() {
        return dataListOptionSearchRepository;
    }

    @Override
    public Function<DataListOptionSearchEntity, UUID> entityGetIdFunction() {
        return DataListOptionSearchEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DataListOptionSearchEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(DataListOptionSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
