package org.twins.core.domain;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentModificationEntity;
import org.twins.core.dao.twin.*;
import org.twins.core.enums.factory.FactoryLauncher;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.history.HistoryCollector;
import org.twins.core.service.history.HistoryCollectorMultiTwin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
public class TwinChangesCollector extends EntitiesChangesCollector {
    private final HistoryCollectorMultiTwin historyCollector = new HistoryCollectorMultiTwin();
    private boolean historyCollectorEnabled = true; // in some cases we do not need to collect history changes (before drafting for example, currently we do not collect history, only after )
    private final Map<Object, Set<TwinInvalidate>> invalidationMap = new ConcurrentHashMap<>();
    private final Map<UUID, Pair<UUID, FactoryLauncher>> postponedChanges = new ConcurrentHashMap<>();
    private final Map<UUID, List<Triple<UUID, UUID, FactoryLauncher>>> postponedTrigger = new ConcurrentHashMap<>();

    public TwinChangesCollector() {
        super();
    }

    public TwinChangesCollector(boolean historyCollectorEnabled) {
        super();
        this.historyCollectorEnabled = historyCollectorEnabled;
    }

    public HistoryCollector getHistoryCollector(TwinEntity twinEntity) {
        return historyCollector.forTwin(twinEntity);
    }

    @Override
    protected ChangesHelper detectChangesHelper(Object entity) {
        markForInvalidate(entity);
        return super.detectChangesHelper(entity);
    }

    @Override
    public void delete(Object entity) {
        markForInvalidate(entity);
        super.delete(entity);
    }

    private void markForInvalidate(Object entity) {
        Set<TwinInvalidate> invalidates;
        if (entity instanceof TwinMarkerEntity twinMarkerEntity) {
            invalidationMap.computeIfAbsent(twinMarkerEntity.getTwin(), k -> ConcurrentHashMap.newKeySet())
                    .add(TwinInvalidate.markersKit);
        } else if (entity instanceof TwinTagEntity twinTagEntity) {
            invalidationMap.computeIfAbsent(twinTagEntity.getTwin(), k -> ConcurrentHashMap.newKeySet())
                    .add(TwinInvalidate.tagsKit);
        } else if (entity instanceof TwinLinkEntity twinLinkEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinLinkEntity.getSrcTwin(), k -> ConcurrentHashMap.newKeySet());
            invalidates.add(TwinInvalidate.twinLinks);
            invalidates.add(TwinInvalidate.fieldValuesKit);
            invalidates = invalidationMap.computeIfAbsent(twinLinkEntity.getDstTwin(), k -> ConcurrentHashMap.newKeySet());
            invalidates.add(TwinInvalidate.twinLinks);
            invalidates.add(TwinInvalidate.fieldValuesKit);
        } else if (entity instanceof TwinFieldSimpleEntity twinFieldSimpleEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinFieldSimpleEntity.getTwin(), k -> ConcurrentHashMap.newKeySet());
            invalidates.add(TwinInvalidate.twinFieldSimpleKit);
            invalidates.add(TwinInvalidate.fieldValuesKit);
        } else if (entity instanceof TwinFieldSimpleNonIndexedEntity twinFieldSimpleNonIndexedEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinFieldSimpleNonIndexedEntity.getTwin(), k -> ConcurrentHashMap.newKeySet());
            invalidates.add(TwinInvalidate.twinFieldSimpleNonIndexedKit);
            invalidates.add(TwinInvalidate.fieldValuesKit);
        } else if (entity instanceof TwinFieldDataListEntity twinFieldDataListEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinFieldDataListEntity.getTwin(), k -> ConcurrentHashMap.newKeySet());
            invalidates.add(TwinInvalidate.twinFieldDatalistKit);
            invalidates.add(TwinInvalidate.fieldValuesKit);
        } else if (entity instanceof TwinFieldUserEntity twinFieldUserEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinFieldUserEntity.getTwin(), k -> ConcurrentHashMap.newKeySet());
            invalidates.add(TwinInvalidate.twinFieldUserKit);
            invalidates.add(TwinInvalidate.fieldValuesKit);
        } else if (entity instanceof TwinAttachmentEntity twinAttachmentEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinAttachmentEntity.getTwin(), k -> ConcurrentHashMap.newKeySet());
            invalidates.add(TwinInvalidate.twinAttachments);;
        } else if (entity instanceof TwinFieldI18nEntity twinFieldI18nEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinFieldI18nEntity.getTwin(), k -> ConcurrentHashMap.newKeySet());
            invalidates.add(TwinInvalidate.twinFieldI18nKit);
        } else if (entity instanceof TwinAttachmentModificationEntity twinAttachmentModificationEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinAttachmentModificationEntity.getTwinAttachment(), k -> ConcurrentHashMap.newKeySet());
            invalidates.add(TwinInvalidate.twinAttachmentModifications);
        } else if (entity instanceof TwinFieldBooleanEntity twinFieldBooleanEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinFieldBooleanEntity.getTwin(), k -> ConcurrentHashMap.newKeySet());
            invalidates.add(TwinInvalidate.twinFieldBooleanKit);
        } else if (entity instanceof TwinFieldTwinClassEntity twinFieldTwinClassEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinFieldTwinClassEntity.getTwin(), k -> ConcurrentHashMap.newKeySet());
            invalidates.add(TwinInvalidate.twinFieldTwinClassKit);
        } else if (entity instanceof TwinFieldAttributeEntity twinFieldAttributeEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinFieldAttributeEntity.getTwin(), k -> ConcurrentHashMap.newKeySet());
            invalidates.add(TwinInvalidate.twinFieldAttributeKit);
        }
    }

    public TwinChangesCollector addPostponedChange(UUID twinId, UUID twinFactoryId, FactoryLauncher factoryLauncher) throws ServiceException {
        if (postponedChanges.containsKey(twinId) && !postponedChanges.get(twinId).getLeft().equals(twinFactoryId)) {
            throw new ServiceException(ErrorCodeTwins.CONFIGURATION_IS_INVALID,
                    "twin[{}] already has postponed changes by factory[{}]. Skipping changes by factory[{}]", twinId, postponedChanges.get(twinId), twinFactoryId);
        }
        postponedChanges.put(twinId, Pair.of(twinFactoryId, factoryLauncher));
        return this;
    }

    public TwinChangesCollector addPostponedTrigger(UUID twinId, UUID twinTriggerId, UUID previousTwinStatusId, FactoryLauncher triggerLauncher) throws ServiceException {
        postponedTrigger.computeIfAbsent(twinId, k -> new ArrayList<>());
        postponedTrigger.get(twinId).add(Triple.of(twinTriggerId, previousTwinStatusId, triggerLauncher));
        return this;
    }


    public void clear() {
        super.clear();
        historyCollector.clear();
    }

    public enum TwinInvalidate {
        twinAttachmentModifications,
        twinAttachments,
        tagsKit,
        markersKit,
        twinFieldSimpleKit,
        twinFieldSimpleNonIndexedKit,
        twinFieldUserKit,
        twinFieldDatalistKit,
        twinFieldI18nKit,
        fieldValuesKit,
        twinFieldBooleanKit,
        twinFieldTwinClassKit,
        twinLinks,
        twinFieldAttributeKit;
    }
}
