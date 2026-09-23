package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

@Component
public class FieldLookuperFromContextTwinFields extends FieldLookuperNearest {

    @Override
    protected void beforeLookup(FactoryItemsBatch batch, TwinClassFieldEntity twinClassField) throws ServiceException {
        if (!batch.getContextTwins().isEmpty())
            twinService.loadTwinFields(batch.getContextTwins(), twinClassField); // one bulk load for the db-fields fallback
    }

    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        FactoryItem contextItem = factoryItem.checkSingleContextItem();
        FieldValue fieldValue = contextItem.getOutput().getField(lookupTwinClassField);
        if (fieldValue != null) {
            return fieldValue;
        }
        fieldValue = getValueFromOutputLinks(lookupTwinClassField, contextItem.getOutput());
        if (fieldValue != null) {
            return fieldValue;
        }
        TwinEntity contextTwin = contextItem.getTwin();
        fieldValue = twinService.getTwinFieldValue(contextTwin, lookupTwinClassField);
        if (fieldValue == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, lookupTwinClassField.logNormal() + " is not present in context twin uncommitted fields and db fields");
        }
        return fieldValue;
    }
}
