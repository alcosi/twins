package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

@Component
public class FieldLookuperFromItemOutputDbFields extends FieldLookuperNearest {

    @Override
    protected void beforeLookup(FactoryItemsBatch batch, TwinClassFieldEntity twinClassField) throws ServiceException {
        twinService.loadTwinFields(batch.getTwins(), twinClassField); // one bulk load for the whole batch
    }

    @Override
    public FieldValue lookupFieldValueOrNull(FactoryItem factoryItem, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        TwinEntity outputTwin = factoryItem.getOutput().getTwinEntity();
        return twinService.getTwinFieldValue(outputTwin, lookupTwinClassField);
    }
}
