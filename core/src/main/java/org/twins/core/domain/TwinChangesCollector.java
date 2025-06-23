package org.twins.core.domain;

import jakarta.persistence.EntityManager;
import lombok.Getter;
import org.cambium.common.util.ChangesHelper;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentModificationEntity;
import org.twins.core.dao.twin.*;
import org.twins.core.service.history.HistoryCollector;
import org.twins.core.service.history.HistoryCollectorMultiTwin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class TwinChangesCollector extends EntitiesChangesCollector {
    private final HistoryCollectorMultiTwin historyCollector = new HistoryCollectorMultiTwin();
    private boolean historyCollectorEnabled = true; // in some cases we do not need to collect history changes (before drafting for example, currently we do not collect history, only after )
    private final Map<Object, Set<TwinInvalidate>> invalidationMap = new HashMap<>();

    public TwinChangesCollector() {
        super();
    }

    public TwinChangesCollector(boolean historyCollectorEnabled) {
        super();
        this.historyCollectorEnabled = historyCollectorEnabled;
    }

    public TwinChangesCollector(boolean detachNeeded, EntityManager em) {
        super(detachNeeded, em);
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
            invalidationMap.computeIfAbsent(twinMarkerEntity.getTwin(), k -> new HashSet<>())
                    .add(TwinInvalidate.markersKit);
        } else if (entity instanceof TwinTagEntity twinTagEntity) {
            invalidationMap.computeIfAbsent(twinTagEntity.getTwin(), k -> new HashSet<>())
                    .add(TwinInvalidate.tagsKit);
        } else if (entity instanceof TwinLinkEntity twinLinkEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinLinkEntity.getSrcTwin(), k -> new HashSet<>());
            invalidates.add(TwinInvalidate.twinLinks);
            invalidates.add(TwinInvalidate.fieldValuesKit);
            invalidates = invalidationMap.computeIfAbsent(twinLinkEntity.getDstTwin(), k -> new HashSet<>());
            invalidates.add(TwinInvalidate.twinLinks);
            invalidates.add(TwinInvalidate.fieldValuesKit);
        } else if (entity instanceof TwinFieldSimpleEntity twinFieldSimpleEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinFieldSimpleEntity.getTwin(), k -> new HashSet<>());
            invalidates.add(TwinInvalidate.twinFieldSimpleKit);
            invalidates.add(TwinInvalidate.fieldValuesKit);
        } else if (entity instanceof TwinFieldSimpleNonIndexedEntity twinFieldSimpleNonIndexedEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinFieldSimpleNonIndexedEntity.getTwin(), k -> new HashSet<>());
            invalidates.add(TwinInvalidate.twinFieldSimpleNonIndexedKit);
            invalidates.add(TwinInvalidate.fieldValuesKit);
        } else if (entity instanceof TwinFieldDataListEntity twinFieldDataListEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinFieldDataListEntity.getTwin(), k -> new HashSet<>());
            invalidates.add(TwinInvalidate.twinFieldDatalistKit);
            invalidates.add(TwinInvalidate.fieldValuesKit);
        } else if (entity instanceof TwinFieldUserEntity twinFieldUserEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinFieldUserEntity.getTwin(), k -> new HashSet<>());
            invalidates.add(TwinInvalidate.twinFieldUserKit);
            invalidates.add(TwinInvalidate.fieldValuesKit);
        } else if (entity instanceof TwinAttachmentEntity twinAttachmentEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinAttachmentEntity.getTwin(), k -> new HashSet<>());
            invalidates.add(TwinInvalidate.twinAttachments);;
        } else if (entity instanceof TwinFieldI18nEntity twinFieldI18nEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinFieldI18nEntity.getTwin(), k -> new HashSet<>());
            invalidates.add(TwinInvalidate.twinFieldI18nKit);
        } else if (entity instanceof TwinAttachmentModificationEntity twinAttachmentModificationEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinAttachmentModificationEntity.getTwinAttachment(), k -> new HashSet<>());
            invalidates.add(TwinInvalidate.twinAttachmentModifications);
        } else if (entity instanceof TwinFieldBooleanEntity twinFieldBooleanEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinFieldBooleanEntity.getTwin(), k -> new HashSet<>());
            invalidates.add(TwinInvalidate.twinFieldBooleanKit);
        }
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
        twinLinks;
    }
}
