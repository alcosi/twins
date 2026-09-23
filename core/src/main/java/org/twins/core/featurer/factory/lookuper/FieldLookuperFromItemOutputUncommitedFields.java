package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinSave;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

@Component
public class FieldLookuperFromItemOutputUncommitedFields extends FieldLookuperNearest {
    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        TwinSave output = factoryItem.getOutput();
        FieldValue fieldValue = output.getField(lookupTwinClassField);
        if (fieldValue == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, lookupTwinClassField.logNormal() + " is not present in output twin uncommited fields");
        return fieldValue;
    }
}
