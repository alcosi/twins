package org.twins.core.service.twinclassfield.recompute;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclassfield.TwinClassFieldRecomputeOnActionValidatorRuleEntity;
import org.twins.core.dao.twinclassfield.TwinClassFieldRecomputeOnActionValidatorRuleRepository;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.twin.TwinValidatorSetService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Secure-find service for {@link TwinClassFieldRecomputeOnActionValidatorRuleEntity} — optional
 * validator_set predicates attached to an OnAction recompute rule ("recompute parent.sum only if
 * the child twin passes validator_set V").
 *
 * The orchestrator batch-loads rules for all surviving OnAction recompute rules in one shot via
 * {@link #findByTwinClassFieldRecomputeOnActionIdIn(Collection)} (cached at the repository level —
 * see {@link TwinClassFieldRecomputeOnActionValidatorRuleRepository#CACHE_BY_RECOMPUTE_ID_IN}).
 *
 * The {@code load*} methods delegate to the base {@link org.cambium.service.EntitySecureFindServiceImpl#load}
 * on the related entity's service — one batch SQL per relationship, no N+1 in mappers / admin UI.
 */
@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFieldRecomputeOnActionValidatorRuleService extends TwinsEntitySecureFindService<TwinClassFieldRecomputeOnActionValidatorRuleEntity> {
    private final TwinClassFieldRecomputeOnActionValidatorRuleRepository repository;
    @Lazy
    private final TwinClassFieldRecomputeOnActionService recomputeOnActionService;
    @Lazy
    private final TwinValidatorSetService twinValidatorSetService;

    @Override
    public CrudRepository<TwinClassFieldRecomputeOnActionValidatorRuleEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinClassFieldRecomputeOnActionValidatorRuleEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldRecomputeOnActionValidatorRuleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldRecomputeOnActionValidatorRuleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFieldRecomputeOnActionValidatorRuleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getId() == null)
            return logErrorAndReturnFalse(entity.logShort() + " empty id");
        if (entity.getTwinClassFieldRecomputeOnActionId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty twinClassFieldRecomputeOnActionId");
        return true;
    }

    public List<TwinClassFieldRecomputeOnActionValidatorRuleEntity> findByTwinClassFieldRecomputeOnActionIdIn(
            Collection<UUID> recomputeActionIds) {
        if (recomputeActionIds == null || recomputeActionIds.isEmpty())
            return List.of();
        return repository.findByTwinClassFieldRecomputeOnActionIdInOrderByOrder(recomputeActionIds);
    }

    public void loadTwinClassFieldRecomputeOnAction(TwinClassFieldRecomputeOnActionValidatorRuleEntity src) throws ServiceException {
        loadTwinClassFieldRecomputeOnAction(Collections.singleton(src));
    }

    public void loadTwinClassFieldRecomputeOnAction(Collection<TwinClassFieldRecomputeOnActionValidatorRuleEntity> srcCollection) throws ServiceException {
        recomputeOnActionService.load(srcCollection,
                TwinClassFieldRecomputeOnActionValidatorRuleEntity::getTwinClassFieldRecomputeOnActionId,
                TwinClassFieldRecomputeOnActionValidatorRuleEntity::getTwinClassFieldRecomputeOnAction,
                TwinClassFieldRecomputeOnActionValidatorRuleEntity::setTwinClassFieldRecomputeOnAction);
    }

    public void loadTwinValidatorSet(TwinClassFieldRecomputeOnActionValidatorRuleEntity src) throws ServiceException {
        loadTwinValidatorSet(Collections.singleton(src));
    }

    public void loadTwinValidatorSet(Collection<TwinClassFieldRecomputeOnActionValidatorRuleEntity> srcCollection) throws ServiceException {
        twinValidatorSetService.load(srcCollection,
                TwinClassFieldRecomputeOnActionValidatorRuleEntity::getTwinValidatorSetId,
                TwinClassFieldRecomputeOnActionValidatorRuleEntity::getTwinValidatorSet,
                TwinClassFieldRecomputeOnActionValidatorRuleEntity::setTwinValidatorSet);
    }
}
