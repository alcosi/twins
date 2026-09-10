package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;

import java.util.UUID;

@Component
public class FieldLookuperFromContextTwinLinkedTwinByFieldDbFields extends FieldLookuperLinkedTwinByField {


    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID linkedTwinByTwinClassFieldId, UUID lookupTwinClassFieldId) throws ServiceException {
        var contextTwin = factoryItem.checkSingleContextTwin();
        twinService.loadFieldsValues(contextTwin);
        var fieldValue = contextTwin.getFieldValuesKit().get(linkedTwinByTwinClassFieldId);
        TwinEntity fromTwin = FieldValueLink.getSingleLinkedTwinSafe(fieldValue);
        FieldValue fieldValueForCopy = twinService.getTwinFieldValue(fromTwin, lookupTwinClassFieldId);
        if (fieldValueForCopy == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + lookupTwinClassFieldId + "] is not present in linked twin fields");
        }

        return fieldValueForCopy;
    }
}
