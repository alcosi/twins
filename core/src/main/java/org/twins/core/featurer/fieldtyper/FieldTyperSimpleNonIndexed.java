package org.twins.core.featurer.fieldtyper;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNonIndexedEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.search.TwinFieldValueSearch;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimpleNonIndex;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.history.HistoryItem;

import java.util.Properties;
import java.util.UUID;

public abstract class FieldTyperSimpleNonIndexed<D extends FieldDescriptor, T extends FieldValue, A extends TwinFieldValueSearch>
        extends FieldTyperSingleValue<D, T, TwinFieldSimpleNonIndexedEntity, String, TwinFieldStorageSimpleNonIndex, A> {

    @Override
    protected void setEntityValue(TwinFieldSimpleNonIndexedEntity twinFieldEntity, String newValue) {
        twinFieldEntity.setValue(newValue);
    }

    @Override
    protected String getEntityValue(TwinFieldSimpleNonIndexedEntity twinFieldEntity) {
        return twinFieldEntity.getValue();
    }

    @Override
    protected Kit<TwinFieldSimpleNonIndexedEntity, UUID> getFieldKit(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldSimpleNonIndexedKit();
    }

    @Override
    protected TwinFieldSimpleNonIndexedEntity createTwinFieldEntity(TwinEntity twin, TwinClassFieldEntity twinClassField) {
        return TwinFieldSimpleNonIndexedEntity.of(twin, twinClassField);
    }

    @Override
    protected HistoryItem<?> createHistoryItem(TwinFieldSimpleNonIndexedEntity twinFieldEntity, String newValue) {
        // Secret/non-indexed fields never record the new value in history (old value hidden too).
        return historyService.fieldChangeSimpleSecret(twinFieldEntity.getTwinClassField(), twinFieldEntity.getValue());
    }

    /**
     * Non-indexed fields nullify on clear: the stored row is kept and its value is set to null.
     */
    @Override
    protected void onCleared(Properties properties, TwinFieldSimpleNonIndexedEntity twinFieldEntity, TwinChangesCollector twinChangesCollector) {
        detectValueChange(twinFieldEntity, twinChangesCollector, null);
    }
}
