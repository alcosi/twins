package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.UUID;

@Component
public class FieldLookuperFromItemOutputDbFields extends FieldLookuperNearest {

    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID lookupTwinClassFieldId) throws ServiceException {
        TwinEntity outputTwin = factoryItem.getOutput().getTwinEntity();
        FieldValue fieldValue = twinService.getTwinFieldValue(outputTwin, lookupTwinClassFieldId);
        if (fieldValue == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + lookupTwinClassFieldId + "] is not present in output twin fields twinclass[" + outputTwin.getTwinClassId() + "]");
        return fieldValue;
    }
}
