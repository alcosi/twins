package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldValueSearch;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.util.Properties;

/**
 * Base for Mater decimal field types (calculated/materialized values stored in
 * {@code twin_field_decimal}). Carries the Mater mechanics — abstract 5-arg {@code serializeValue}
 * / 3-arg {@code deserializeValue} hooks, the 4-arg/2-arg resolution entry points, value-change
 * detection and history — while the numeric formatting parameters, {@code processAndFormatValue}
 * and {@code deserializeValueBase} come from {@link FieldTyperNumeric} (shared with the standalone
 * {@link FieldTyperDecimal}).
 */
public abstract class FieldTyperDecimalBase<D extends FieldDescriptor, T extends FieldValue, A extends TwinFieldValueSearch>
        extends FieldTyper<D, T, TwinFieldStorageDecimal, A>
        implements FieldTyperNumeric {

    @Override
    public boolean isUnchangedUpdate(TwinEntity twin, T value) throws ServiceException {
        if (value.isUndefined() || value.isCleared() || !(value instanceof FieldValueText text))
            return false;
        if (twin.getTwinFieldDecimalKit() == null && !ensureFieldStorageLoaded(twin, value.getTwinClassField()))
            return false;
        if (twin.getTwinFieldDecimalKit() == null)
            return false;
        TwinFieldDecimalEntity entity = twin.getTwinFieldDecimalKit().get(value.getTwinClassFieldId());
        if (entity == null || entity.getValue() == null)
            return false;
        try {
            Properties properties = featurerService.extractProperties(this, value.getTwinClassField().getFieldTyperParams());
            BigDecimal incoming = new BigDecimal(processAndFormatValue(properties, text));
            return incoming.compareTo(entity.getValue()) == 0;
        } catch (ServiceException | RuntimeException e) {
            return false;
        }
    }

    protected abstract void serializeValue(Properties properties, TwinEntity twin, TwinFieldDecimalEntity twinFieldEntity, T value, TwinChangesCollector twinChangesCollector) throws ServiceException;
    protected abstract T deserializeValue(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldDecimalEntity) throws ServiceException;

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        var twinFieldEntity = resolveTwinFieldEntity(twin, value.getTwinClassField());
        serializeValue(properties, twin, twinFieldEntity, value, twinChangesCollector);
    }

    @Override
    protected T deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        var twinFieldDecimalEntity = resolveTwinFieldEntity(twinField.getTwin(), twinField.getTwinClassField());
        return deserializeValue(properties, twinField, twinFieldDecimalEntity);
    }

    private TwinFieldDecimalEntity resolveTwinFieldEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        return twinEntity.getTwinFieldDecimalKit().get(twinClassFieldEntity.getId());
    }

    protected void detectValueChange(TwinFieldDecimalEntity twinFieldDecimalEntity, TwinChangesCollector twinChangesCollector, BigDecimal newValue) {
        if (twinChangesCollector.collectIfChangedWithNullifySupport(twinFieldDecimalEntity, "field[" + twinFieldDecimalEntity.getTwinClassField().getKey() + "]", twinFieldDecimalEntity.getValue(), newValue)) {
            addHistoryContext(twinChangesCollector, twinFieldDecimalEntity, newValue);
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
