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
    public boolean checkIsChanged(TwinEntity twin, T oldValue, T newValue, Properties properties) throws ServiceException {
        if (!(oldValue instanceof FieldValueText stored)
                || stored.getValue() == null
                || !(newValue instanceof FieldValueText incomingText))
            return true;
        try {
            BigDecimal incoming = new BigDecimal(processAndFormatValue(properties, incomingText));
            return incoming.compareTo(new BigDecimal(stored.getValue())) != 0;
        } catch (ServiceException | RuntimeException e) {
            // A value that cannot be normalized is not an echo of storage. Permission and validation still apply.
            return true;
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

    protected void collectChangedValue(TwinFieldDecimalEntity twinFieldDecimalEntity, TwinChangesCollector twinChangesCollector, BigDecimal newValue) {
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
