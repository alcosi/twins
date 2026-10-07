package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

@Component
public class FieldLookuperFromContextTwinFieldsOnly extends FieldLookuperNearest {

    @Override
    public FieldValue lookupFieldValueOrNull(FactoryItem factoryItem, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        FactoryItem contextItem = factoryItem.checkSingleContextItem();
        var fieldValue = contextItem.getOutput().getField(lookupTwinClassField);
        if (fieldValue != null) {
            return fieldValue;
        }
        return getValueFromOutputLinks(lookupTwinClassField, contextItem.getOutput());
    }
}
