package org.twins.core.service.trigger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.trigger.TwinTriggerRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class TwinTriggerService extends EntitySecureFindServiceImpl<TwinTriggerEntity> {
    @Getter
    private final TwinTriggerRepository repository;
    private final AuthService authService;

    @Override
    public CrudRepository<TwinTriggerEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinTriggerEntity, UUID> entityGetIdFunction() {
        return TwinTriggerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinTriggerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return entity.getDomainId() != null && !entity.getDomainId().equals(apiUser.getDomainId());
    }

    @Override
    public boolean validateEntity(TwinTriggerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return !isEntityReadDenied(entity, EntitySmartService.ReadPermissionCheckMode.none);
    }
}
