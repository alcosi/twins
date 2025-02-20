package org.twins.core.service.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dao.resource.StorageRepository;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class StorageService extends EntitySecureFindServiceImpl<StorageEntity> {
    protected final StorageRepository repository;
    protected final AuthService authService;

    @Override
    public CrudRepository<StorageEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<StorageEntity, UUID> entityGetIdFunction() {
        return StorageEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(StorageEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = entity.getDomainId() != null && !entity.getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.logNormal() + " is not allowed in" + domain.logShort());
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(StorageEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

}
