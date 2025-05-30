package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNonIndexedEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Properties;

public abstract class FieldTyperSimpleNonIndexed<D extends FieldDescriptor, T extends FieldValue, A extends TwinFieldSearch> extends FieldTyper<D, T, TwinFieldSimpleNonIndexedEntity, A> {

    protected void detectValueChange(TwinFieldSimpleNonIndexedEntity twinFieldSimpleNonIndexedEntity, TwinChangesCollector twinChangesCollector, String newValue) {
        if (twinChangesCollector.collectIfChanged(twinFieldSimpleNonIndexedEntity, "field[" + twinFieldSimpleNonIndexedEntity.getTwinClassField().getKey() + "]", twinFieldSimpleNonIndexedEntity.getValue(), newValue)) {
            if (twinChangesCollector.isHistoryCollectorEnabled())
                twinChangesCollector.getHistoryCollector(twinFieldSimpleNonIndexedEntity.getTwin()).add(
                        historyService.fieldChangeSimple(twinFieldSimpleNonIndexedEntity.getTwinClassField(), twinFieldSimpleNonIndexedEntity.getValue(), newValue));
            twinFieldSimpleNonIndexedEntity.setValue(newValue);
        }
    }

    public TwinFieldSimpleNonIndexedEntity convertToTwinFieldEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        twinService.loadTwinFields(twinEntity);
        return twinEntity.getTwinFieldSimpleNonIndexedKit().get(twinClassFieldEntity.getId());
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinFieldSimpleNonIndexedEntity twinFieldSimpleNonIndexedEntity = convertToTwinFieldEntity(twin, value.getTwinClassField());
        if (twinFieldSimpleNonIndexedEntity == null) {
            twinFieldSimpleNonIndexedEntity = twinService.createTwinFieldNonIndexedEntity(twin, value.getTwinClassField(), null);
            twinChangesCollector.add(twinFieldSimpleNonIndexedEntity);
        }
        serializeValue(properties, twinFieldSimpleNonIndexedEntity, value, twinChangesCollector);
    }

    protected abstract void serializeValue(Properties properties, TwinFieldSimpleNonIndexedEntity twinFieldEntity, T value, TwinChangesCollector twinChangesCollector) throws ServiceException;

    @Override
    protected T deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinFieldSimpleNonIndexedEntity twinFieldSimpleNonIndexedEntity = convertToTwinFieldEntity(twinField.getTwin(), twinField.getTwinClassField());
        return deserializeValue(properties, twinField, twinFieldSimpleNonIndexedEntity);
    }

    protected abstract T deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleNonIndexedEntity twinFieldEntity) throws ServiceException;

}
