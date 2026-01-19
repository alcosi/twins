package org.twins.core.featurer.fieldtyper;

import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;
import java.util.UUID;

public abstract class FieldTyperCalcBinaryBase<S extends TwinFieldStorageSimple> extends FieldTyper<FieldDescriptorText, FieldValueText, S, TwinFieldSearchNotImplemented> implements FieldTyperCalcBinary {

    @Override
    protected FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorText();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    protected abstract String calculate(Double v1, Double v2, Properties properties) throws ServiceException;

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        Double firstValue = getValues(twinField.getTwin(), firstFieldId.extract(properties));
        Double secondValue = getValues(twinField.getTwin(), secondFieldId.extract(properties));

        String result = calculate(firstValue, secondValue, properties);

        return new FieldValueText(twinField.getTwinClassField()).setValue(result);
    }

    private Double getValues(TwinEntity twin, UUID fieldId) throws ServiceException {
        if (twin.getTwinFieldSimpleKit() != null && twin.getTwinFieldSimpleKit().containsKey(fieldId)) {
            TwinFieldSimpleEntity field = twin.getTwinFieldSimpleKit().get(fieldId);
            try {
                if (field.getValue() != null) {
                    return Double.parseDouble(field.getValue());
                }
                ;
            } catch (NumberFormatException e) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, field.easyLog(EasyLoggable.Level.NORMAL) + " value[" + field.getValue() + "] cant be parsed to Long");
            }
        }
        return null;
    }
}
