package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

@Component
public class FieldLookuperFromContextTwinDbFields extends FieldLookuperNearest {

    @Override
    protected void beforeLookup(FactoryItemsBatch batch, TwinClassFieldEntity twinClassField) throws ServiceException {
        if (!batch.getContextTwins().isEmpty())
            twinService.loadTwinFields(batch.getContextTwins(), twinClassField); // one bulk load for the whole batch
    }

    @Override
    public FieldValue lookupFieldValueOrNull(FactoryItem factoryItem, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        return twinService.getTwinFieldValue(twinService.wrapField(factoryItem.checkSingleContextTwin(), lookupTwinClassField));
    }
}
