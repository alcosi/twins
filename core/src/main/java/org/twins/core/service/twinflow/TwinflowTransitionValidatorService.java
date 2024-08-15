package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinflow.TwinflowTransitionValidatorEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionValidatorRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowTransitionValidatorService extends EntitySecureFindServiceImpl<TwinflowTransitionValidatorEntity> {
    private final TwinflowTransitionValidatorRepository twinflowTransitionValidatorRepository;
    @Override
    public CrudRepository<TwinflowTransitionValidatorEntity, UUID> entityRepository() {
        return twinflowTransitionValidatorRepository;
    }

    @Override
    public Function<TwinflowTransitionValidatorEntity, UUID> entityGetIdFunction() {
        return TwinflowTransitionValidatorEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinflowTransitionValidatorEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinflowTransitionValidatorEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
