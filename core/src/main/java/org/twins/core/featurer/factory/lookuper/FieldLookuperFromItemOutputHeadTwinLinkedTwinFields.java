package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;

import java.util.UUID;

@Component
public class FieldLookuperFromItemOutputHeadTwinLinkedTwinFields extends FieldLookuperLinkedTwinByField {
    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID linkedTwinByTwinClassFieldId, UUID lookupTwinClassFieldId) throws ServiceException {
        TwinEntity headTwin = twinService.loadHeadForTwin(factoryItem.getTwin());
        if (headTwin == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + lookupTwinClassFieldId + "] can not be loaded from head twin, because head is null");
        FieldValue itemOutputHeadTwinField = getFreshestValue(headTwin, linkedTwinByTwinClassFieldId, factoryItem.getFactoryContext(), "TwinClassField[" + lookupTwinClassFieldId + "] is not present in output item head fields");
        if (!(itemOutputHeadTwinField instanceof FieldValueLink itemOutputHeadTwinFieldLink))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + linkedTwinByTwinClassFieldId + "] is not of type link");
        if (itemOutputHeadTwinFieldLink.isEmpty())
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + linkedTwinByTwinClassFieldId + "] is empty for head " + headTwin);
        if (itemOutputHeadTwinFieldLink.size() > 1)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + linkedTwinByTwinClassFieldId + "] has " + itemOutputHeadTwinFieldLink.size() +  " linked twins in  " + headTwin);
        TwinEntity linkDstTwin = twinLinkService.getDstTwinSafe(itemOutputHeadTwinFieldLink.getItems().getFirst());
        return getFreshestValue(linkDstTwin, lookupTwinClassFieldId, factoryItem.getFactoryContext(), "TwinClassField[" + lookupTwinClassFieldId + "] is not present in output item head linked twin fields");
    }
}
