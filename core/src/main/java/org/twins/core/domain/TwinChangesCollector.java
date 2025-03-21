package org.twins.core.domain;

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
    private final Map<TwinEntity, Set<TwinInvalidate>> invalidationMap = new HashMap<>();

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
        } else if (entity instanceof TwinAttachmentModificationEntity twinAttachmentModificationEntity) {
            invalidates = invalidationMap.computeIfAbsent(twinAttachmentModificationEntity.getTwinAttachment().getTwin(), k -> new HashSet<>());
            invalidates.add(TwinInvalidate.twinAttachmentModifications);;
        }
    }


    public void clear() {
        super.clear();
        historyCollector.clear();
    }

    public enum TwinInvalidate {
        twinAttachmentModifications(1),
        twinAttachments(2),
        tagsKit(3),
        markersKit(4),
        twinFieldSimpleKit(5),
        twinFieldUserKit(6),
        twinFieldDatalistKit(7),
        twinLinks(8),
        fieldValuesKit(9);

        @Getter
        private final int order;

        TwinInvalidate(int order) {
            this.order = order;
        }
    }
}
