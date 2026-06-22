package org.twins.core.service.twinflow;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleRepository;
import org.twins.core.service.twin.TwinValidatorSetService;
import org.twins.core.service.validator.TwinValidatorService;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinflowTransitionValidatorService extends EntitySecureFindServiceImpl<TwinflowTransitionValidatorRuleEntity> {
    private final TwinflowTransitionValidatorRuleRepository twinflowTransitionValidatorRuleRepository;
    @Lazy
    private final TwinValidatorSetService twinValidatorSetService;
    @Lazy
    private final TwinValidatorService twinValidatorService;

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

    public void loadTwinValidatorSet(TwinflowTransitionValidatorRuleEntity src) throws ServiceException {
        twinValidatorSetService.loadTwinValidatorSet(src);
    }

    public void loadTwinValidatorSet(Collection<TwinflowTransitionValidatorRuleEntity> srcCollection) throws ServiceException {
        twinValidatorSetService.loadTwinValidatorSet(srcCollection);
    }

    public void loadValidators(TwinflowTransitionValidatorRuleEntity src) throws ServiceException {
        twinValidatorService.loadValidators(src);
    }

    public void loadValidators(Collection<TwinflowTransitionValidatorRuleEntity> srcCollection) throws ServiceException {
        twinValidatorService.loadValidators(srcCollection);
    }
}
