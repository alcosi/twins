package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleRepository;
import org.twins.core.service.validator.TwinValidatorService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinPointerValidatorRuleService extends EntitySecureFindServiceImpl<TwinPointerValidatorRuleEntity> {
    private final TwinPointerValidatorRuleRepository twinPointerValidatorRuleRepository;
    private final TwinPointerService twinPointerService;
    private final TwinValidatorSetService twinValidatorSetService;
    @Lazy
    private final TwinValidatorService twinValidatorService;

    @Override
    public CrudRepository<TwinPointerValidatorRuleEntity, UUID> entityRepository() {
        return twinPointerValidatorRuleRepository;
    }

    @Override
    public Function<TwinPointerValidatorRuleEntity, UUID> entityGetIdFunction() {
        return TwinPointerValidatorRuleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinPointerValidatorRuleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) {
        return twinPointerService.isEntityReadDenied(entity.getTwinPointer());
    }

    @Override
    public boolean validateEntity(TwinPointerValidatorRuleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) {
        if (entity.getId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty id");
        if (entity.getTwinPointer() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty faceTwinPointer");
        if (entity.getTwinValidatorSetId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty twinValidatorSetId");
        return true;
    }

    public boolean isValid(TwinEntity currentTwin, UUID twinPointerValidatorRuleId) throws ServiceException {
        TwinPointerValidatorRuleEntity pointerValidatorRuleEntity = twinPointerValidatorRuleRepository.findById(twinPointerValidatorRuleId)
                .orElseThrow(() -> new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION));
        TwinEntity pointedTwin = twinPointerService.getPointer(currentTwin, pointerValidatorRuleEntity.getTwinPointerId());
        return twinValidatorSetService.isValid(pointedTwin, pointerValidatorRuleEntity);
    }
}
