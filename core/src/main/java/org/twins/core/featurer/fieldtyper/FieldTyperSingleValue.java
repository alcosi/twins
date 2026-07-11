package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBaseEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.search.TwinFieldValueSearch;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageMater;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.history.HistoryItem;

import java.util.Properties;
import java.util.UUID;

public abstract class FieldTyperSingleValue<
        D extends FieldDescriptor,
        T extends FieldValue,
        E extends TwinFieldBaseEntity,
        V,
        S extends TwinFieldStorageMater<E>,
        A extends TwinFieldValueSearch> extends FieldTyper<D, T, S, A> {
    protected void detectValueChange(E twinFieldEntity, TwinChangesCollector twinChangesCollector, V newValue) {
        var oldValue = getEntityValue(twinFieldEntity);
        if (twinChangesCollector.collectIfChangedWithNullifySupport(twinFieldEntity, "field[" + twinFieldEntity.getTwinClassField().getKey() + "]", oldValue, newValue)) {
            addHistoryContext(twinChangesCollector, twinFieldEntity, newValue);
            setEntityValue(twinFieldEntity, newValue);
        }
    }

    protected abstract void setEntityValue(E twinFieldEntity, V newValue);

    protected abstract V getEntityValue(E twinFieldEntity);

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (value.isUndefined())
            return;
        var twinFieldEntity = resolveTwinFieldEntity(twin, value.getTwinClassField());
        if (twinFieldEntity == null && value.isNotEmpty()) {
            // create
            twinFieldEntity = createTwinFieldEntity(twin, value.getTwinClassField());
            detectValueChange(twinFieldEntity, twinChangesCollector, processValue(properties, twinFieldEntity, value));
        } else if (twinFieldEntity != null && value.isCleared()) {
            onCleared(properties, twinFieldEntity, twinChangesCollector);
        } else if (twinFieldEntity != null && value.isNotEmpty()) {
            // update
            detectValueChange(twinFieldEntity, twinChangesCollector, processValue(properties, twinFieldEntity, value));
        }
    }

    public E resolveTwinFieldEntity(TwinEntity twin, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        return getFieldKit(twin).get(twinClassFieldEntity.getId());
    }

    protected abstract Kit<E, UUID> getFieldKit(TwinEntity twinEntity);

    protected abstract E createTwinFieldEntity(TwinEntity twin, TwinClassFieldEntity twinClassField);

    protected abstract V processValue(Properties properties, E twinFieldEntity, T value) throws ServiceException;

    protected void addHistoryContext(TwinChangesCollector twinChangesCollector, E twinFieldEntity, V newValue) {
        if (twinChangesCollector.isHistoryCollectorEnabled()) {
            twinChangesCollector
                    .getHistoryCollector(twinFieldEntity.getTwin())
                    .add(createHistoryItem(twinFieldEntity, newValue));
        }
    }

    protected abstract HistoryItem<?> createHistoryItem(E twinFieldEntity, V newValue);

    /**
     * Persisting strategy for a cleared (explicitly nulled) value when a stored row already exists.
     * Default: delete the row — semantics of {@link FieldTyperTimestamp} / decimal fields. Override
     * to nullify the value in place (keep the row, set null — {@link FieldTyperSimple} /
     * {@link FieldTyperSimpleNonIndexed}) or to substitute a default value ({@link FieldTyperBoolean}).
     */
    protected void onCleared(Properties properties, E twinFieldEntity, TwinChangesCollector twinChangesCollector) {
        twinChangesCollector.delete(twinFieldEntity);
        addHistoryContext(twinChangesCollector, twinFieldEntity, null);
    }
}
