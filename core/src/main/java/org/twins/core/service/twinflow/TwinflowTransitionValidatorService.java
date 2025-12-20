package org.twins.core.service.twinflow;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinflowTransitionValidatorService extends EntitySecureFindServiceImpl<TwinflowTransitionValidatorRuleEntity> {
    private final TwinflowTransitionValidatorRuleRepository twinflowTransitionValidatorRuleRepository;
    @Override
    public CrudRepository<TwinflowTransitionValidatorRuleEntity, UUID> entityRepository() {
        return twinflowTransitionValidatorRuleRepository;
    }

    @Override
    public Function<TwinflowTransitionValidatorRuleEntity, UUID> entityGetIdFunction() {
        return TwinflowTransitionValidatorRuleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinflowTransitionValidatorRuleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinflowTransitionValidatorRuleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
