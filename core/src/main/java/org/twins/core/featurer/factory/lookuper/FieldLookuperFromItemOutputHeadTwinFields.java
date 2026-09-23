package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.ArrayList;
import java.util.List;

@Component
public class FieldLookuperFromItemOutputHeadTwinFields extends FieldLookuperNearest {

    @Override
    protected void beforeLookup(FactoryItemsBatch batch, TwinClassFieldEntity twinClassField) throws ServiceException {
        twinService.loadHead(batch.getTwins()); // one bulk load for the whole batch
        List<TwinEntity> headTwins = new ArrayList<>(batch.getTwins().size());
        for (TwinEntity twin : batch.getTwins()) {
            if (twin.getHeadTwin() != null)
                headTwins.add(twin.getHeadTwin());
        }
        if (!headTwins.isEmpty())
            twinService.loadTwinFields(headTwins, twinClassField); // one bulk load of the head fields
    }

    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        TwinEntity headTwin = twinService.loadHead(factoryItem.getTwin());
        if (headTwin == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, lookupTwinClassField.logNormal() + " can not be loaded from head twin, because head is null");
        return getFreshestValue(headTwin, lookupTwinClassField, factoryItem.getFactoryContext(), lookupTwinClassField.logNormal() + " is not present in head twin fields");
    }
}
