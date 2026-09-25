package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

@Component
public class FieldLookuperFromItemOutputFields extends FieldLookuperNearest {

    @Override
    protected void beforeLookup(FactoryItemsBatch batch, TwinClassFieldEntity twinClassField) throws ServiceException {
        twinService.loadTwinFields(batch.getTwins(), twinClassField); // one bulk load for the db fallback of getFreshestValue
    }

    @Override
    public FieldValue lookupFieldValueOrNull(FactoryItem factoryItem, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        return getFreshestValue(factoryItem.getTwin(), lookupTwinClassField, factoryItem.getFactoryContext());
    }
}
