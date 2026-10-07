package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

@Component
public class FieldLookuperFromItemOutputUncommitedFields extends FieldLookuperNearest {
    @Override
    public FieldValue lookupFieldValueOrNull(FactoryItem factoryItem, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        return factoryItem.getOutput().getField(lookupTwinClassField);
    }
}
