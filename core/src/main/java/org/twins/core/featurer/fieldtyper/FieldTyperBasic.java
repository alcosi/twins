package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Properties;

public abstract class FieldTyperBasic<D extends FieldDescriptor, T extends FieldValue> extends FieldTyper<D, T, TwinFieldEntity> {
    protected void detectValueChange(TwinFieldEntity twinFieldEntity, TwinChangesCollector twinChangesCollector, String newValue) {
        if (twinChangesCollector.isChanged(twinFieldEntity, "field[" + twinFieldEntity.getTwinClassField().getKey() + "]", twinFieldEntity.getValue(), newValue)) {
            twinChangesCollector.getHistoryCollector(twinFieldEntity.getTwin()).add(
                    historyService.fieldChangeSimple(twinFieldEntity.getTwinClassField(), twinFieldEntity.getValue(), newValue));
            twinFieldEntity.setValue(newValue);
        }
    }

    public TwinFieldEntity convertToTwinFieldEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        twinService.loadTwinFields(twinEntity); // loading field kits,  in case of new twin fields serialization this will create one more dummy query to DB
        return twinEntity.getTwinFieldBasicKit().get(twinClassFieldEntity.getId());
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinFieldEntity twinFieldEntity = convertToTwinFieldEntity(twin, value.getTwinClassField());
        if (twinFieldEntity == null) {
            twinFieldEntity = twinService.createTwinFieldEntity(twin, value.getTwinClassField(), null);
            twinChangesCollector.add(twinFieldEntity);
        }
        serializeValue(properties, twinFieldEntity, value, twinChangesCollector);
    }

    protected abstract void serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, T value, TwinChangesCollector twinChangesCollector) throws ServiceException;

    @Override
    protected T deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinFieldEntity twinFieldEntity = convertToTwinFieldEntity(twinField.getTwin(), twinField.getTwinClassField());
        return deserializeValue(properties, twinField, twinFieldEntity);
    }

    protected abstract T deserializeValue(Properties properties, TwinField twinField, TwinFieldEntity twinFieldEntity) throws ServiceException;


}
