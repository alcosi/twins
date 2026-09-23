package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.UUID;

@Component
public class FieldLookuperFromContextTwinDbFields extends FieldLookuperNearest {

    @Override
    protected void beforeLookup(FactoryItemsBatch batch, TwinClassFieldEntity twinClassField) throws ServiceException {
        if (!batch.getContextTwins().isEmpty())
            twinService.loadTwinFields(batch.getContextTwins(), twinClassField); // one bulk load for the whole batch
    }

    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID lookupTwinClassFieldId) throws ServiceException {
        FieldValue fieldValue = twinService.getTwinFieldValue(twinService.wrapField(factoryItem.checkSingleContextTwin(), lookupTwinClassFieldId));
        if (fieldValue == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "lookupTwinClassFieldId[" + lookupTwinClassFieldId + "] is not present in context twin db fields");
        return fieldValue;
    }
}
