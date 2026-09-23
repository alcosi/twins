package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

@Component
public class FieldLookuperFromContextTwinDbFieldsAndContextFields extends FieldLookuperNearest {

    @Override
    protected void beforeLookup(FactoryItemsBatch batch, TwinClassFieldEntity twinClassField) throws ServiceException {
        var contextTwins = batch.getContextTwins(2); // level 2: the per-item lookup goes "deeper" into the context item's own context twin
        if (!contextTwins.isEmpty())
            twinService.loadTwinFields(contextTwins, twinClassField); // one bulk load covering both lookup levels
    }

    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID lookupTwinClassFieldId) throws ServiceException {
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
        FieldValue fieldValue = twinService.getTwinFieldValue(contextTwin, lookupTwinClassFieldId);
        if (TwinService.isFilled(fieldValue))
            return fieldValue;
        // we will try to look deeper
        contextTwin = factoryItem.checkSingleContextItem().checkSingleContextTwin();
        fieldValue = twinService.getTwinFieldValue(contextTwin, lookupTwinClassFieldId);
        if (TwinService.isFilled(fieldValue))
            return fieldValue;
        // we will look inside context fields
        fieldValue = factoryItem.getFactoryContext().getFields().get(lookupTwinClassFieldId);
        if (!TwinService.isFilled(fieldValue))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + lookupTwinClassFieldId + "] is not present in context fields and in context twins");
        return fieldValue;
    }
}
