package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Properties;


public abstract class FieldTyperCalcBinaryBase extends FieldTyper<FieldDescriptorImmutable, FieldValueText, TwinFieldStorageSimple, TwinFieldSearchNotImplemented> implements FieldTyperCalcBinary {

    @Autowired
    private TwinClassFieldService twinClassFieldService;

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
        Double firstValue = twinClassFieldService.parseNumericField(twinField.getTwin(), firstFieldId.extract(properties), 0.0);
        Double secondValue = twinClassFieldService.parseNumericField(twinField.getTwin(), secondFieldId.extract(properties), 0.0);

        String result = calculate(firstValue, secondValue, properties);

        return new FieldValueText(twinField.getTwinClassField()).setValue(result);
    }
}
