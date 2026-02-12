package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.util.Properties;


public abstract class FieldTyperCalcBinaryBase extends FieldTyperImmutable<FieldDescriptorImmutable, FieldValueText, TwinFieldStorageSimple, TwinFieldSearchNotImplemented> implements FieldTyperCalcBinary {

    @Override
    protected FieldDescriptorImmutable getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorImmutable();
    }

    protected abstract String calculate(BigDecimal v1, BigDecimal v2, Properties properties) throws ServiceException;

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        BigDecimal firstValue = FieldTyperNumeric.parseBigDecimalValue(twinField.getTwin(), firstFieldId.extract(properties), BigDecimal.ZERO);
        BigDecimal secondValue = FieldTyperNumeric.parseBigDecimalValue(twinField.getTwin(), secondFieldId.extract(properties), BigDecimal.ZERO);

        String result = calculate(firstValue, secondValue, properties);

        return new FieldValueText(twinField.getTwinClassField()).setValue(result);
    }
}
