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
import org.twins.core.dao.recompute.TwinRecomputeOnActionValidatorRuleEntity;
import org.twins.core.dao.recompute.TwinRecomputeOnActionValidatorRuleRepository;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.twin.TwinValidatorSetService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Secure-find service for {@link TwinRecomputeOnActionValidatorRuleEntity} — optional validator_set
 * predicates attached to an OnAction recompute rule ("recompute only if the publisher twin passes
 * validator_set V"). The orchestrator batch-loads rules for OnAction recompute rules via
 * {@link #findByTwinRecomputeOnActionIdIn(Collection)} (cached at the repository level — see
 * {@link TwinRecomputeOnActionValidatorRuleRepository#CACHE_BY_RECOMPUTE_ID_IN}).
 *
 * The {@code load*} methods delegate to the base {@link org.cambium.service.EntitySecureFindServiceImpl#load}
 * on the related entity's service — one batch SQL per relationship, no N+1.
 */
@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinRecomputeOnActionValidatorRuleService extends TwinsEntitySecureFindService<TwinRecomputeOnActionValidatorRuleEntity> {
    private final TwinRecomputeOnActionValidatorRuleRepository repository;
    @Lazy
    private final TwinRecomputeOnActionService twinRecomputeOnActionService;
    @Lazy
    private final TwinValidatorSetService twinValidatorSetService;

    @Override
    public CrudRepository<TwinRecomputeOnActionValidatorRuleEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinRecomputeOnActionValidatorRuleEntity, UUID> entityGetIdFunction() {
        return TwinRecomputeOnActionValidatorRuleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinRecomputeOnActionValidatorRuleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinRecomputeOnActionValidatorRuleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getId() == null)
            return logErrorAndReturnFalse(entity.logShort() + " empty id");
        if (entity.getTwinRecomputeOnActionId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty twinRecomputeOnActionId");
        return true;
    }

    public List<TwinRecomputeOnActionValidatorRuleEntity> findByTwinRecomputeOnActionIdIn(
            Collection<UUID> recomputeActionIds) {
        if (recomputeActionIds == null || recomputeActionIds.isEmpty())
            return List.of();
        return repository.findByTwinRecomputeOnActionIdInOrderByOrder(recomputeActionIds);
    }

    public void loadTwinRecomputeOnAction(TwinRecomputeOnActionValidatorRuleEntity src) throws ServiceException {
        loadTwinRecomputeOnAction(Collections.singleton(src));
    }

    public void loadTwinRecomputeOnAction(Collection<TwinRecomputeOnActionValidatorRuleEntity> srcCollection) throws ServiceException {
        twinRecomputeOnActionService.load(srcCollection,
                TwinRecomputeOnActionValidatorRuleEntity::getTwinRecomputeOnActionId,
                TwinRecomputeOnActionValidatorRuleEntity::getTwinRecomputeOnAction,
                TwinRecomputeOnActionValidatorRuleEntity::setTwinRecomputeOnAction);
    }

    public void loadTwinValidatorSet(TwinRecomputeOnActionValidatorRuleEntity src) throws ServiceException {
        loadTwinValidatorSet(Collections.singleton(src));
    }

    public void loadTwinValidatorSet(Collection<TwinRecomputeOnActionValidatorRuleEntity> srcCollection) throws ServiceException {
        twinValidatorSetService.load(srcCollection,
                TwinRecomputeOnActionValidatorRuleEntity::getTwinValidatorSetId,
                TwinRecomputeOnActionValidatorRuleEntity::getTwinValidatorSet,
                TwinRecomputeOnActionValidatorRuleEntity::setTwinValidatorSet);
    }
}
