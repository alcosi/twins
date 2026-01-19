package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;


public abstract class FieldTyperCalcBinaryBase extends FieldTyper<FieldDescriptorImmutable, FieldValueText, TwinFieldStorageSimple, TwinFieldSearchNotImplemented> implements FieldTyperCalcBinary {

    @Override
    protected FieldDescriptorImmutable getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorImmutable();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    protected abstract String calculate(Double v1, Double v2, Properties properties) throws ServiceException;

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        Double firstValue = FieldTyperNumeric.parseDoubleValue(twinField.getTwin(), firstFieldId.extract(properties), 0.0);
        Double secondValue = FieldTyperNumeric.parseDoubleValue(twinField.getTwin(), secondFieldId.extract(properties), 0.0);

        String result = calculate(firstValue, secondValue, properties);

        return new FieldValueText(twinField.getTwinClassField()).setValue(result);
    }
}
