package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.math.BigDecimal;
import java.util.Properties;

public abstract class FieldTyperDecimalBase<D extends FieldDescriptor, T extends FieldValue, A extends TwinFieldSearch> extends FieldTyper<D, T, TwinFieldStorageDecimal, A> {

    protected abstract void serializeValue(Properties properties, TwinEntity twin, TwinFieldDecimalEntity twinFieldEntity, T value, TwinChangesCollector twinChangesCollector) throws ServiceException;
    protected abstract T deserializeValue(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldDecimalEntity) throws ServiceException;

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        var twinFieldDecimalEntity = convertToTwinFieldEntity(twin, value.getTwinClassField());
        serializeValue(properties, twin, twinFieldDecimalEntity, value, twinChangesCollector);
    }

    @Override
    protected T deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        var twinFieldDecimalEntity = convertToTwinFieldEntity(twinField.getTwin(), twinField.getTwinClassField());
        return deserializeValue(properties, twinField, twinFieldDecimalEntity);
    }

    private TwinFieldDecimalEntity convertToTwinFieldEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        twinService.loadTwinFields(twinEntity);
        return twinEntity.getTwinFieldDecimalKit().get(twinClassFieldEntity.getId());
    }

    protected void detectValueChange(TwinFieldDecimalEntity twinFieldDecimalEntity, TwinChangesCollector twinChangesCollector, BigDecimal newValue) {
        if (twinChangesCollector.collectIfChanged(twinFieldDecimalEntity, "field[" + twinFieldDecimalEntity.getTwinClassField().getKey() + "]", twinFieldDecimalEntity.getValue(), newValue)) {
            addHistoryContext(twinChangesCollector,  twinFieldDecimalEntity, newValue);
            twinFieldDecimalEntity.setValue(newValue);
        }
    }

    protected void addHistoryContext(TwinChangesCollector twinChangesCollector, TwinFieldDecimalEntity twinFieldDecimalEntity, BigDecimal newValue) {
        if (twinChangesCollector.isHistoryCollectorEnabled()) {
            twinChangesCollector
                    .getHistoryCollector(twinFieldDecimalEntity.getTwin())
                    .add(
                            historyService.fieldChangeDecimal(
                                    twinFieldDecimalEntity.getTwinClassField(),
                                    twinFieldDecimalEntity.getValue() != null ? twinFieldDecimalEntity.getValue() : null,
                                    newValue
                            )
                    );
        }
    }
}

