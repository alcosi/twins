package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowTransitionTriggerService extends EntitySecureFindServiceImpl<TwinflowTransitionTriggerEntity> {
    private final TwinflowTransitionTriggerRepository twinflowTransitionTriggerRepository;
    @Override
    public CrudRepository<TwinflowTransitionTriggerEntity, UUID> entityRepository() {
        return twinflowTransitionTriggerRepository;
    }

    @Override
    public Function<TwinflowTransitionTriggerEntity, UUID> entityGetIdFunction() {
        return TwinflowTransitionTriggerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinflowTransitionTriggerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinflowTransitionTriggerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
