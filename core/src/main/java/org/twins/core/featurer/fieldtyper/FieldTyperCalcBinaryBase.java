package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.math.BigDecimal;
import java.util.Properties;


public abstract class FieldTyperCalcBinaryBase extends FieldTyperImmutable<FieldDescriptorImmutable, FieldValueText, TwinFieldStorageDecimal, TwinFieldSearchNotImplemented> implements FieldTyperCalcBinary {

    @Autowired
    private TwinClassFieldService twinClassFieldService;

    @Override
    protected FieldDescriptorImmutable getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorImmutable();
    }

    protected abstract String calculate(BigDecimal v1, BigDecimal v2, Properties properties) throws ServiceException;

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        var firstValue = twinClassFieldService.getDecimalValue(twinField.getTwin(), firstFieldId.extract(properties), BigDecimal.ZERO);
        var secondValue = twinClassFieldService.getDecimalValue(twinField.getTwin(), secondFieldId.extract(properties), BigDecimal.ZERO);

        var result = calculate(firstValue, secondValue, properties);

        return new FieldValueText(twinField.getTwinClassField()).setValue(result);
    }
}
