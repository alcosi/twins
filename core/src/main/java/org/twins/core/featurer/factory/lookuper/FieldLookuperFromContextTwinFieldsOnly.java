package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.UUID;

@Component
public class FieldLookuperFromContextTwinFieldsOnly extends FieldLookuperNearest {

    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID lookupTwinClassFieldId) throws ServiceException {
        FactoryItem contextItem = factoryItem.checkSingleContextItem();
        return contextItem.getOutput().getField(lookupTwinClassFieldId);
    }
}
