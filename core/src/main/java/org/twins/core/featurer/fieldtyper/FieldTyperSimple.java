package org.twins.core.featurer.fieldtyper;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.search.TwinFieldValueSearch;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.history.HistoryItem;

import java.util.Properties;
import java.util.UUID;

public abstract class FieldTyperSimple<D extends FieldDescriptor, T extends FieldValue, A extends TwinFieldValueSearch>
        extends FieldTyperSingleValue<D, T, TwinFieldSimpleEntity, String, TwinFieldStorageSimple, A> {

    @Override
    protected void setEntityValue(TwinFieldSimpleEntity twinFieldEntity, String newValue) {
        twinFieldEntity.setValue(newValue);
    }

    @Override
    protected String getEntityValue(TwinFieldSimpleEntity twinFieldEntity) {
        return twinFieldEntity.getValue();
    }

    @Override
    protected Kit<TwinFieldSimpleEntity, UUID> getFieldKit(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldSimpleKit();
    }

    @Override
    protected TwinFieldSimpleEntity createTwinFieldEntity(TwinEntity twin, TwinClassFieldEntity twinClassField) {
        return TwinFieldSimpleEntity.of(twin, twinClassField);
    }

    @Override
    protected HistoryItem<?> createHistoryItem(TwinFieldSimpleEntity twinFieldEntity, String newValue) {
        return historyService.fieldChangeSimple(
                twinFieldEntity.getTwinClassField(),
                twinFieldEntity.getValue(),
                newValue);
    }

    /**
     * Simple fields nullify on clear: the stored row is kept and its value is set to null
     * (not deleted) — matches the former collectIfChangedWithNullifySupport(null) behaviour.
     */
    @Override
    protected void onCleared(Properties properties, TwinFieldSimpleEntity twinFieldEntity, TwinChangesCollector twinChangesCollector) {
        detectValueChange(twinFieldEntity, twinChangesCollector, null);
    }
}
