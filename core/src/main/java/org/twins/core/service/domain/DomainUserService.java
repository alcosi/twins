package org.twins.core.service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.domain.DomainUserRepository;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class DomainUserService extends EntitySecureFindServiceImpl<DomainUserEntity> {
    @Getter
    private final DomainUserRepository repository;
    private final AuthService authService;

    @Override
    public CrudRepository<DomainUserEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<DomainUserEntity, UUID> entityGetIdFunction() {
        return DomainUserEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DomainUserEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allowed in " + domain.logShort());
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(DomainUserEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
