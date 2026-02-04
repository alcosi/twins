package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;


public abstract class FieldTyperCalcBinaryBase extends FieldTyper<FieldDescriptorImmutable, FieldValueText, TwinFieldStorageDecimal, TwinFieldSearchNotImplemented> implements FieldTyperCalcBinary {

    @Override
    protected FieldDescriptorImmutable getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorImmutable();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    protected abstract String calculate(BigDecimal v1, BigDecimal v2, Properties properties) throws ServiceException;

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        var firstValue = getDecimalValue(twinField.getTwin(), firstFieldId.extract(properties), BigDecimal.ZERO);
        var secondValue = getDecimalValue(twinField.getTwin(), secondFieldId.extract(properties), BigDecimal.ZERO);

        var result = calculate(firstValue, secondValue, properties);

        return new FieldValueText(twinField.getTwinClassField()).setValue(result);
    }

    protected BigDecimal getDecimalValue(TwinEntity twin, UUID fieldId, BigDecimal defaultValue) {
        if (twin.getTwinFieldDecimalKit() != null && twin.getTwinFieldDecimalKit().containsKey(fieldId)) {
            var field = twin.getTwinFieldDecimalKit().get(fieldId);

            if (field.getValue() != null) {
                return field.getValue();
            }
        }

        return defaultValue;
    }
}
