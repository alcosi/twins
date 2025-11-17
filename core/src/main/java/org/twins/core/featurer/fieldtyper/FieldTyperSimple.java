package org.twins.core.featurer.fieldtyper;

import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Properties;

public abstract class FieldTyperSimple<D extends FieldDescriptor, T extends FieldValue, A extends TwinFieldSearch> extends FieldTyper<D, T, TwinFieldStorageSimple, A> {

    protected void detectValueChange(TwinFieldSimpleEntity twinFieldEntity, TwinChangesCollector twinChangesCollector, String newValue) {
        if (twinChangesCollector.collectIfChanged(twinFieldEntity, "field[" + twinFieldEntity.getTwinClassField().getKey() + "]", twinFieldEntity.getValue(), newValue)) {
            if (twinChangesCollector.isHistoryCollectorEnabled())
                twinChangesCollector.getHistoryCollector(twinFieldEntity.getTwin()).add(
                        historyService.fieldChangeSimple(twinFieldEntity.getTwinClassField(), twinFieldEntity.getValue(), newValue));
            twinFieldEntity.setValue(newValue);
        }
    }

    public TwinFieldSimpleEntity convertToTwinFieldEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        twinService.loadTwinFields(twinEntity); // loading field kits,  in case of new twin fields serialization this will create one more dummy query to DB
        return twinEntity.getTwinFieldSimpleKit().get(twinClassFieldEntity.getId());
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinFieldSimpleEntity twinFieldEntity = convertToTwinFieldEntity(twin, value.getTwinClassField());
        if (twinFieldEntity == null) {
            twinFieldEntity = twinService.createTwinFieldEntity(twin, value.getTwinClassField(), null);
            twinChangesCollector.add(twinFieldEntity);
        }
        serializeValue(properties, twinFieldEntity, value, twinChangesCollector);
    }

    protected abstract void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, T value, TwinChangesCollector twinChangesCollector) throws ServiceException;

    @Override
    protected T deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinFieldSimpleEntity twinFieldEntity = convertToTwinFieldEntity(twinField.getTwin(), twinField.getTwinClassField());
        return deserializeValue(properties, twinField, twinFieldEntity);
    }

    protected abstract T deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleEntity twinFieldEntity) throws ServiceException;

    @Override
    public ValidationResult validate(Properties properties, TwinEntity twin, T fieldValue) throws ServiceException {
        return new ValidationResult(true);
    }


}
