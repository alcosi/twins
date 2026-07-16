package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.UUID;

@Component
public class FieldLookuperFromContextTwinFields extends FieldLookuperNearest {

    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID lookupTwinClassFieldId) throws ServiceException {
        FactoryItem contextItem = factoryItem.checkSingleContextItem();
        FieldValue fieldValue = contextItem.getOutput().getField(lookupTwinClassFieldId);
        if (fieldValue != null) {
            return fieldValue;
        }
        TwinEntity contextTwin = contextItem.getTwin();
        fieldValue = twinService.getTwinFieldValue(contextTwin, lookupTwinClassFieldId);
        if (fieldValue == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + lookupTwinClassFieldId + "] is not present in context twin uncommitted fields and db fields");
        }
        return fieldValue;
    }
}
