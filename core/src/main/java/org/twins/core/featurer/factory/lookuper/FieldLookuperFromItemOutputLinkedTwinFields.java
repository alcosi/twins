package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;

import java.util.UUID;

@Component
public class FieldLookuperFromItemOutputLinkedTwinFields extends FieldLookuperLinkedTwinByField {
    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID linkedTwinByTwinClassFieldId, UUID lookupTwinClassFieldId) throws ServiceException {
        TwinEntity twinEntity = factoryItem.getTwin();
        FieldValue itemOutputField = getFreshestValue(twinEntity, linkedTwinByTwinClassFieldId, factoryItem.getFactoryContext(), "TwinClassField[" + lookupTwinClassFieldId + "] is not present in output item fields");
        TwinEntity linkDstTwin = FieldValueLink.getSingleLinkedTwinSafe(itemOutputField);
        return getFreshestValue(linkDstTwin, lookupTwinClassFieldId, factoryItem.getFactoryContext(), "TwinClassField[" + lookupTwinClassFieldId + "] is not present in output item linked twin fields");
    }
}
