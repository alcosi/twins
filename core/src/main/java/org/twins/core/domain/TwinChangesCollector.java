package org.twins.core.domain;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentModificationEntity;
import org.twins.core.dao.twin.*;
import org.twins.core.enums.factory.FactoryLauncher;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.history.HistoryCollector;
import org.twins.core.service.history.HistoryCollectorMultiTwin;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@Getter
public class TwinChangesCollector extends EntitiesChangesCollector {
    private final HistoryCollectorMultiTwin historyCollector = new HistoryCollectorMultiTwin();
    private boolean historyCollectorEnabled = true; // in some cases we do not need to collect history changes (before drafting for example, currently we do not collect history, only after )
    private final Map<Object, Set<TwinInvalidate>> invalidationMap = new ConcurrentHashMap<>();
    private final Map<UUID, Pair<UUID, FactoryLauncher>> postponedChanges = new ConcurrentHashMap<>();
    private final PostponedTriggers postponedTriggers = new PostponedTriggers();

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
    protected ChangesHelper detectChangesHelper(Identifiable entity) {
        syncRelations(entity, true);
        return super.detectChangesHelper(entity);
    }

    @Override
    public void delete(Identifiable entity) {
        syncRelations(entity, false);
        super.delete(entity);
    }

    private void syncRelations(Identifiable entity, boolean saveElseDelete) {
        Set<TwinInvalidate> invalidates;
        if (entity instanceof TwinMarkerEntity twinMarkerEntity) {
            invalidateTwin(twinMarkerEntity.getTwin(), TwinInvalidate.markersKit);
        } else if (entity instanceof TwinTagEntity twinTagEntity) {
            invalidateTwin(twinTagEntity.getTwin(), TwinInvalidate.tagsKit);
        } else if (entity instanceof TwinLinkEntity twinLinkEntity) {
            invalidateTwin(twinLinkEntity.getSrcTwin(), TwinInvalidate.twinLinks, TwinInvalidate.fieldValuesKit);
            invalidateTwin(twinLinkEntity.getDstTwin(), TwinInvalidate.twinLinks, TwinInvalidate.fieldValuesKit);
        } else if (entity instanceof TwinAttachmentEntity twinAttachmentEntity) {
            invalidateTwin(twinAttachmentEntity.getTwin(), TwinInvalidate.twinAttachments);
        } else if (entity instanceof TwinAttachmentModificationEntity twinAttachmentModificationEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinAttachmentModificationEntity.getTwinAttachment(), k -> ConcurrentHashMap.newKeySet());
            invalidates.add(TwinInvalidate.twinAttachmentModifications);
        } else if (entity instanceof TwinFieldAttributeEntity twinFieldAttributeEntity) {
            invalidateTwin(twinFieldAttributeEntity.getTwin(), TwinInvalidate.twinFieldAttributeKit);
        } else if (entity instanceof TwinFieldSimpleEntity twinFieldSimpleEntity) {
            syncFieldKitAndInvalidate(
                    twinFieldSimpleEntity,
                    saveElseDelete,
                    TwinEntity::getTwinFieldSimpleKit);
        } else if (entity instanceof TwinFieldSimpleNonIndexedEntity twinFieldSimpleNonIndexedEntity) {
            syncFieldKitAndInvalidate(
                    twinFieldSimpleNonIndexedEntity,
                    saveElseDelete,
                    TwinEntity::getTwinFieldSimpleNonIndexedKit);
        } else if (entity instanceof TwinFieldDataListEntity twinFieldDataListEntity) {
            syncFieldKitAndInvalidate(
                    twinFieldDataListEntity,
                    saveElseDelete,
                    TwinEntity::getTwinFieldDatalistKit);
        } else if (entity instanceof TwinFieldUserEntity twinFieldUserEntity) {
            syncFieldKitAndInvalidate(
                    twinFieldUserEntity,
                    saveElseDelete,
                    TwinEntity::getTwinFieldUserKit);
        } else if (entity instanceof TwinFieldI18nEntity twinFieldI18nEntity) {
            syncFieldKitAndInvalidate(
                    twinFieldI18nEntity,
                    saveElseDelete,
                    TwinEntity::getTwinFieldI18nKit);
        } else if (entity instanceof TwinFieldBooleanEntity twinFieldBooleanEntity) {
            syncFieldKitAndInvalidate(
                    twinFieldBooleanEntity,
                    saveElseDelete,
                    TwinEntity::getTwinFieldBooleanKit);
        } else if (entity instanceof TwinFieldTwinClassEntity twinFieldTwinClassEntity) {
            syncFieldKitAndInvalidate(
                    twinFieldTwinClassEntity,
                    saveElseDelete,
                    TwinEntity::getTwinFieldTwinClassKit);
        } else if (entity instanceof TwinFieldDecimalEntity twinFieldDecimalEntity) {
            syncFieldKitAndInvalidate(
                    twinFieldDecimalEntity,
                    saveElseDelete,
                    TwinEntity::getTwinFieldDecimalKit);
        } else if (entity instanceof TwinFieldTimestampEntity twinFieldTimestampEntity) {
            syncFieldKitAndInvalidate(
                    twinFieldTimestampEntity,
                    saveElseDelete,
                    TwinEntity::getTwinFieldTimestampKit);
        }
    }

    /**
     * Marks the given twin for cache/kit invalidation with the supplied {@link TwinInvalidate} flags.
     * Replaces the repeated {@code invalidationMap.computeIfAbsent(...).add(...)} boilerplate.
     */
    private void invalidateTwin(TwinEntity twin, TwinInvalidate... invalidations) {
        Set<TwinInvalidate> set = invalidationMap.computeIfAbsent(twin, k -> ConcurrentHashMap.newKeySet());
        Collections.addAll(set, invalidations);
    }

    /**
     * Keeps a twin's field kit in sync on save/delete and registers the matching invalidation.
     * Common to every {@link TwinFieldBaseEntity} subclass whose twin exposes a typed kit accessor.
     */
    private <T extends TwinFieldBaseEntity> void syncFieldKitAndInvalidate(
            T field, boolean saveElseDelete, Function<TwinEntity, Kit<T, UUID>> kitExtractor) {
        TwinEntity twin = field.getTwin();
        Kit<T, UUID> kit = kitExtractor.apply(twin);
        if (saveElseDelete) {
            kit.add(field);
        } else {
            kit.removeByKey(field.getId());
        }
        invalidateTwin(twin, TwinInvalidate.fieldValuesKit);
    }

    public TwinChangesCollector addPostponedChange(UUID twinId, UUID twinFactoryId, FactoryLauncher factoryLauncher) throws ServiceException {
        if (postponedChanges.containsKey(twinId) && !postponedChanges.get(twinId).getLeft().equals(twinFactoryId)) {
            throw new ServiceException(ErrorCodeTwins.CONFIGURATION_IS_INVALID,
                    "twin[{}] already has postponed changes by factory[{}]. Skipping changes by factory[{}]", twinId, postponedChanges.get(twinId), twinFactoryId);
        }
        postponedChanges.put(twinId, Pair.of(twinFactoryId, factoryLauncher));
        return this;
    }

    public TwinChangesCollector addPostponedTrigger(UUID twinId, UUID previousTwinStatusId, UUID twinTriggerId) throws ServiceException {
        postponedTriggers.add(twinId, previousTwinStatusId, twinTriggerId);
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
        twinFieldAttributeKit,
        twinFieldTimestampKit,
        twinFieldDecimalKit
    }
}
