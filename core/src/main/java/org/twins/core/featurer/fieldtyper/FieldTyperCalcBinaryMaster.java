package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldValueSearchNumeric;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.util.Properties;

public abstract class FieldTyperCalcBinaryMaster extends FieldTyperDecimalBase<FieldDescriptorNumeric, FieldValueText, TwinFieldValueSearchNumeric> implements FieldTyperCalcBinary {

    @Override
    public FieldDescriptorNumeric getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorNumeric();
    }

    protected abstract BigDecimal calculate(BigDecimal v1, BigDecimal v2, Properties properties) throws ServiceException;

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, TwinFieldDecimalEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinFieldEntity == null) {
            twinFieldEntity = twinService.createTwinFieldDecimalEntity(twin, value.getTwinClassField(), null);
            twinChangesCollector.add(twinFieldEntity);
        }
        var firstValue = twinClassFieldService.getDecimalValue(twin, firstFieldId.extract(properties), BigDecimal.ZERO);
        var secondValue = twinClassFieldService.getDecimalValue(twin, secondFieldId.extract(properties), BigDecimal.ZERO);
        detectValueChange(twinFieldEntity, twinChangesCollector, calculate(firstValue, secondValue, properties));
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldEntity) throws ServiceException {
        return deserializeValueBase(properties, twinField, twinFieldEntity);
    }
}
