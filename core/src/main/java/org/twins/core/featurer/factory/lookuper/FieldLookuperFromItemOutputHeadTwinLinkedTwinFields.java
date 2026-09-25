package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FieldLookuperFromItemOutputHeadTwinLinkedTwinFields extends FieldLookuperLinkedTwinByField {

    @Override
    protected void beforeLookup(FactoryItemsBatch batch) throws ServiceException {
        twinService.loadHead(batch.getTwins()); // one bulk load for the whole batch
        List<TwinEntity> headTwins = new ArrayList<>(batch.getTwins().size());
        for (TwinEntity twin : batch.getTwins()) {
            if (twin.getHeadTwin() != null)
                headTwins.add(twin.getHeadTwin());
        }
        if (!headTwins.isEmpty())
            twinService.loadTwinFields(headTwins); // one bulk load of the head fields; the linked twin lookup stays per item
    }

    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID linkedTwinByTwinClassFieldId, UUID lookupTwinClassFieldId) throws ServiceException {
        TwinEntity headTwin = twinService.loadHead(factoryItem.getTwin());
        if (headTwin == null) // structural error — the twin has no head to look into, not a missing value
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + lookupTwinClassFieldId + "] can not be loaded from head twin, because head is null");
        FieldValue itemOutputHeadTwinField = getFreshestValue(headTwin, linkedTwinByTwinClassFieldId, factoryItem.getFactoryContext());
        if (itemOutputHeadTwinField == null) // navigation field missing — the lookuper cannot even reach the linked twin
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + lookupTwinClassFieldId + "] is not present in output item head fields");
        TwinEntity linkDstTwin = FieldValueLink.getSingleLinkedTwinSafe(itemOutputHeadTwinField);
        return getFreshestValue(linkDstTwin, lookupTwinClassFieldId, factoryItem.getFactoryContext()); // null when not found — batch entry turns it into an undefined value
    }
}
