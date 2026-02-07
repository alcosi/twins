package org.twins.core.service.trigger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskRepository;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;
import java.util.function.Function;

@Getter
@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class TwinTriggerTaskService extends EntitySecureFindServiceImpl<TwinTriggerTaskEntity> {
    private final TwinTriggerTaskRepository repository;

    @Override
    public CrudRepository<TwinTriggerTaskEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinTriggerTaskEntity, UUID> entityGetIdFunction() {
        return TwinTriggerTaskEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinTriggerTaskEntity entity, org.cambium.service.EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinTriggerTaskEntity entity, org.cambium.service.EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return !isEntityReadDenied(entity, org.cambium.service.EntitySmartService.ReadPermissionCheckMode.none);
    }
}
