package org.twins.core.service.recompute;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.recompute.TwinRecomputeOnFieldValidatorRuleEntity;
import org.twins.core.dao.recompute.TwinRecomputeOnFieldValidatorRuleRepository;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.twinvalidator.TwinValidatorSetService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Secure-find service for {@link TwinRecomputeOnFieldValidatorRuleEntity} — optional validator_set
 * predicates attached to an OnField recompute rule ("recompute only if the publisher twin passes
 * validator_set V"). The orchestrator batch-loads rules for OnField recompute rules via
 * {@link #findByTwinRecomputeOnFieldIdIn(Collection)} (cached at the repository level — see
 * {@link TwinRecomputeOnFieldValidatorRuleRepository#CACHE_BY_RECOMPUTE_ID_IN}).
 *
 * The {@code load*} methods delegate to the base {@link org.cambium.service.EntitySecureFindServiceImpl#load}
 * on the related entity's service — one batch SQL per relationship, no N+1.
 */
@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinRecomputeOnFieldValidatorRuleService extends TwinsEntitySecureFindService<TwinRecomputeOnFieldValidatorRuleEntity> {
    private final TwinRecomputeOnFieldValidatorRuleRepository repository;
    @Lazy
    private final TwinRecomputeOnFieldService twinRecomputeOnFieldService;
    @Lazy
    private final TwinValidatorSetService twinValidatorSetService;

    @Override
    public CrudRepository<TwinRecomputeOnFieldValidatorRuleEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinRecomputeOnFieldValidatorRuleEntity, UUID> entityGetIdFunction() {
        return TwinRecomputeOnFieldValidatorRuleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinRecomputeOnFieldValidatorRuleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinRecomputeOnFieldValidatorRuleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getId() == null)
            return logErrorAndReturnFalse(entity.logShort() + " empty id");
        if (entity.getTwinRecomputeOnFieldId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty twinRecomputeOnFieldId");
        return true;
    }

    public List<TwinRecomputeOnFieldValidatorRuleEntity> findByTwinRecomputeOnFieldIdIn(
            Collection<UUID> recomputeFieldIds) {
        if (recomputeFieldIds == null || recomputeFieldIds.isEmpty())
            return List.of();
        return repository.findByTwinRecomputeOnFieldIdInOrderByOrder(recomputeFieldIds);
    }

    public void loadTwinRecomputeOnField(TwinRecomputeOnFieldValidatorRuleEntity src) throws ServiceException {
        loadTwinRecomputeOnField(Collections.singleton(src));
    }

    public void loadTwinRecomputeOnField(Collection<TwinRecomputeOnFieldValidatorRuleEntity> srcCollection) throws ServiceException {
        twinRecomputeOnFieldService.load(srcCollection,
                TwinRecomputeOnFieldValidatorRuleEntity::getTwinRecomputeOnFieldId,
                TwinRecomputeOnFieldValidatorRuleEntity::getTwinRecomputeOnField,
                TwinRecomputeOnFieldValidatorRuleEntity::setTwinRecomputeOnField);
    }

    public void loadTwinValidatorSet(TwinRecomputeOnFieldValidatorRuleEntity src) throws ServiceException {
        loadTwinValidatorSet(Collections.singleton(src));
    }

    public void loadTwinValidatorSet(Collection<TwinRecomputeOnFieldValidatorRuleEntity> srcCollection) throws ServiceException {
        twinValidatorSetService.load(srcCollection,
                TwinRecomputeOnFieldValidatorRuleEntity::getTwinValidatorSetId,
                TwinRecomputeOnFieldValidatorRuleEntity::getTwinValidatorSet,
                TwinRecomputeOnFieldValidatorRuleEntity::setTwinValidatorSet);
    }
}
