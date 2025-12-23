package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageBoolean;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Properties;

public abstract class FieldTyperBoolean<D extends FieldDescriptor, T extends FieldValue, A extends TwinFieldSearch> extends FieldTyper<D, T, TwinFieldStorageBoolean, A> {

    protected void detectValueChange(TwinFieldBooleanEntity twinFieldBooleanEntity, TwinChangesCollector twinChangesCollector, Boolean newValue) {
        if (twinChangesCollector.collectIfChanged(twinFieldBooleanEntity, "field[" + twinFieldBooleanEntity.getTwinClassField().getKey() + "]", twinFieldBooleanEntity.getValue(), newValue)) {
            if (twinChangesCollector.isHistoryCollectorEnabled())
                twinChangesCollector.getHistoryCollector(twinFieldBooleanEntity.getTwin()).add(
                        historyService.fieldChangeSimple(
                                twinFieldBooleanEntity.getTwinClassField(),
                                twinFieldBooleanEntity.getValue() != null ? twinFieldBooleanEntity.getValue().toString() : null,
                                newValue != null ? newValue.toString() : null)
                );
            twinFieldBooleanEntity.setValue(newValue);
        }
    }

    public TwinFieldBooleanEntity convertToTwinFieldEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        twinService.loadTwinFields(twinEntity);
        return twinEntity.getTwinFieldBooleanKit().get(twinClassFieldEntity.getId());
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinFieldBooleanEntity twinFieldBooleanEntity = convertToTwinFieldEntity(twin, value.getTwinClassField());

        if (twinFieldBooleanEntity == null) {
            twinFieldBooleanEntity = twinService.createTwinFieldBooleanEntity(twin, value.getTwinClassField(), null);
            twinChangesCollector.add(twinFieldBooleanEntity);
        }

        serializeValue(properties, twinFieldBooleanEntity, value, twinChangesCollector);
    }

    protected abstract void serializeValue(Properties properties, TwinFieldBooleanEntity twinFieldEntity, T value, TwinChangesCollector twinChangesCollector) throws ServiceException;

    @Override
    protected T deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinFieldBooleanEntity twinFieldBooleanEntity = convertToTwinFieldEntity(twinField.getTwin(), twinField.getTwinClassField());
        return deserializeValue(properties, twinField, twinFieldBooleanEntity);
    }

    protected abstract T deserializeValue(Properties properties, TwinField twinField, TwinFieldBooleanEntity twinFieldBooleanEntity) throws ServiceException;
}
