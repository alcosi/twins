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
public class FieldLookuperFromItemOutputLinkedTwinHeadTwinFields extends FieldLookuperLinkedTwinByField {
    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID linkedTwinByTwinClassFieldId, UUID lookupTwinClassFieldId) throws ServiceException {
        TwinEntity twinEntity = factoryItem.getTwin();
        FieldValue itemOutputField = getFreshestValue(twinEntity, linkedTwinByTwinClassFieldId, factoryItem.getFactoryContext(), "TwinClassField[" + lookupTwinClassFieldId + "] is not present in output item fields");
        TwinEntity itemOutputLinkedTwin = FieldValueLink.getSingleLinkedTwinSafe(itemOutputField);
        TwinEntity headTwin = twinService.loadHead(itemOutputLinkedTwin);
        if (headTwin == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + lookupTwinClassFieldId + "] can not be loaded from item output linked twin head twin, because head is null");
        return getFreshestValue(headTwin, lookupTwinClassFieldId, factoryItem.getFactoryContext(), "TwinClassField[" + lookupTwinClassFieldId + "] is not present in output item head fields");
    }
}
