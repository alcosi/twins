package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldTimestampEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTimestamp;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.sql.Timestamp;
import java.util.Properties;

public abstract class FieldTyperTimestampBase<D extends FieldDescriptor, T extends FieldValue, A extends TwinFieldSearch> extends FieldTyper<D, T, TwinFieldStorageTimestamp, A> {

    protected void detectValueChange(TwinFieldTimestampEntity twinFieldTimestampEntity, TwinChangesCollector twinChangesCollector, Timestamp newValue) {
        if (twinChangesCollector.collectIfChanged(twinFieldTimestampEntity, "field[" + twinFieldTimestampEntity.getTwinClassField().getKey() + "]", twinFieldTimestampEntity.getValue(), newValue)) {
            if (twinChangesCollector.isHistoryCollectorEnabled()) {
                twinChangesCollector
                        .getHistoryCollector(twinFieldTimestampEntity.getTwin())
                        .add(
                                historyService.fieldChangeTimestamp(
                                        twinFieldTimestampEntity.getTwinClassField(),
                                        twinFieldTimestampEntity.getValue() != null ? twinFieldTimestampEntity.getValue() : null,
                                        newValue
                                )
                        );
            }

            twinFieldTimestampEntity.setValue(newValue);
        }
    }

    public TwinFieldTimestampEntity convertToTwinFieldEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        twinService.loadTwinFields(twinEntity);
        return twinEntity.getTwinFieldTimestampKit().get(twinClassFieldEntity.getId());
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        var twinFieldTimestampEntity = convertToTwinFieldEntity(twin, value.getTwinClassField());

        if (twinFieldTimestampEntity == null) {
            twinFieldTimestampEntity = twinService.createTwinFieldTimestampEntity(twin, value.getTwinClassField(), null);
            twinChangesCollector.add(twinFieldTimestampEntity);
        }

        serializeValue(properties, twinFieldTimestampEntity, value, twinChangesCollector);
    }

    protected abstract void serializeValue(Properties properties, TwinFieldTimestampEntity twinFieldEntity, T value, TwinChangesCollector twinChangesCollector) throws ServiceException;

    @Override
    protected T deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinFieldTimestampEntity twinFieldTimestampEntity = convertToTwinFieldEntity(twinField.getTwin(), twinField.getTwinClassField());
        return deserializeValue(properties, twinField, twinFieldTimestampEntity);
    }

    protected abstract T deserializeValue(Properties properties, TwinField twinField, TwinFieldTimestampEntity twinFieldTimestampEntity) throws ServiceException;
}
