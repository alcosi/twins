package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.UUID;

@Component
public class FieldLookuperFromItemOutputFields extends FieldLookuperNearest {
    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID lookupTwinClassFieldId) throws ServiceException {
        return getFreshestValue(factoryItem.getTwin(), lookupTwinClassFieldId, factoryItem.getFactoryContext(), "TwinClassField[" + lookupTwinClassFieldId + "] is not present in item output fields");
    }
}
