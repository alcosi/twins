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
import org.twins.core.dao.recompute.*;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Secure-find service for {@link TwinRecomputeOnFieldEntity} — OnField recompute rules. Hot-path lookup
 * {@link #findByPublisherTwinClassFieldIdIn(Collection)} is cached at the repository level
 * (see {@link TwinRecomputeOnFieldRepository#CACHE_BY_PUBLISHER_FIELD_IN}); this service is the thin
 * secure-find wrapper the orchestrator goes through.
 *
 * The {@code load*} methods delegate to the base {@link org.cambium.service.EntitySecureFindServiceImpl#load}
 * on the related entity's service — one batch SQL per relationship, no N+1.
 */
@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinRecomputeOnFieldService extends TwinsEntitySecureFindService<TwinRecomputeOnFieldEntity> {
    private final TwinRecomputeOnFieldRepository repository;
    private final TwinRecomputeOnFieldValidatorRuleRepository twinRecomputeOnFieldValidatorRuleRepository;
    @Lazy
    private final TwinRecomputeSubscriberService twinRecomputeSubscriberService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public CrudRepository<TwinRecomputeOnFieldEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinRecomputeOnFieldEntity, UUID> entityGetIdFunction() {
        return TwinRecomputeOnFieldEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinRecomputeOnFieldEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinRecomputeOnFieldEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getId() == null)
            return logErrorAndReturnFalse(entity.logShort() + " empty id");
        if (entity.getRecomputeSubscriberId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty recomputeSubscriberId");
        if (entity.getPublisherTwinClassFieldId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty publisherTwinClassFieldId");
        return true;
    }

    public List<TwinRecomputeOnFieldEntity> findByPublisherTwinClassFieldIdIn(Collection<UUID> publisherFieldIds) {
        if (publisherFieldIds == null || publisherFieldIds.isEmpty())
            return List.of();
        return repository.findByPublisherTwinClassFieldIdIn(publisherFieldIds);
    }

    public void loadSubscriber(TwinRecomputeOnFieldEntity src) throws ServiceException {
        loadSubscriber(Collections.singleton(src));
    }

    /**
     * Attaches the subscriber parent to each rule (one batch SQL via the base {@code load} helper on
     * {@link TwinRecomputeSubscriberService}), then hydrates the distinct subscribers' twin pointer +
     * twin class field in one batch SQL each.
     */
    public void loadSubscriber(Collection<TwinRecomputeOnFieldEntity> srcCollection) throws ServiceException {
        twinRecomputeSubscriberService.load(srcCollection,
                TwinRecomputeOnFieldEntity::getRecomputeSubscriberId,
                TwinRecomputeOnFieldEntity::getSubscriber,
                TwinRecomputeOnFieldEntity::setSubscriber);
        List<TwinRecomputeSubscriberEntity> subscribers = srcCollection.stream()
                .map(TwinRecomputeOnFieldEntity::getSubscriber)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (subscribers.isEmpty())
            return;
        twinRecomputeSubscriberService.loadSubscriberTwinPointer(subscribers);
        twinRecomputeSubscriberService.loadSubscriberTwinClassField(subscribers);
    }

    public void loadPublisherTwinClassField(TwinRecomputeOnFieldEntity src) throws ServiceException {
        loadPublisherTwinClassField(Collections.singleton(src));
    }

    public void loadPublisherTwinClassField(Collection<TwinRecomputeOnFieldEntity> srcCollection) throws ServiceException {
        twinClassFieldService.load(srcCollection,
                TwinRecomputeOnFieldEntity::getPublisherTwinClassFieldId,
                TwinRecomputeOnFieldEntity::getPublisherTwinClassField,
                TwinRecomputeOnFieldEntity::setPublisherTwinClassField);
    }

    public void loadValidators(Collection<TwinRecomputeOnFieldEntity> srcCollection) {
        loadKit(srcCollection,
                TwinRecomputeOnFieldEntity::getId,
                TwinRecomputeOnFieldEntity::getValidatorRulesKit,
                TwinRecomputeOnFieldEntity::setValidatorRulesKit,
                twinRecomputeOnFieldValidatorRuleRepository::findByTwinRecomputeOnFieldIdInOrderByOrder,
                TwinRecomputeOnFieldValidatorRuleEntity::getId,
                TwinRecomputeOnFieldValidatorRuleEntity::getTwinRecomputeOnFieldId,
                TwinRecomputeOnFieldValidatorRuleEntity::setTwinRecomputeOnField);
    }
}
