package org.twins.core.service.resource;

import lombok.RequiredArgsConstructor;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.resource.ResourceStorageEntity;
import org.twins.core.dao.resource.ResourceStorageRepository;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;
import java.util.function.Function;
@Service
@RequiredArgsConstructor
public class ResourceStorageService extends EntitySecureFindServiceImpl<ResourceStorageEntity> {
    protected final ResourceStorageRepository repository;
    protected final AuthService authService;


    @Override
    public CrudRepository<ResourceStorageEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<ResourceStorageEntity, UUID> entityGetIdFunction() {
        return ResourceStorageEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(ResourceStorageEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = false;
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(ResourceStorageEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return !isEntityReadDenied(entity, EntitySmartService.ReadPermissionCheckMode.none);
    }

}
