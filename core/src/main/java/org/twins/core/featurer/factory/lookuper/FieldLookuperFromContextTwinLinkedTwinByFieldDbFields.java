package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;

import java.util.UUID;

@Component
public class FieldLookuperFromContextTwinLinkedTwinByFieldDbFields extends FieldLookuperLinkedTwinByField {

    @Override
    protected void beforeLookup(FactoryItemsBatch batch) throws ServiceException {
        if (!batch.getContextTwins().isEmpty())
            twinService.loadFieldsValues(batch.getContextTwins()); // one bulk load for the whole batch; the linked twin itself stays a per-item lookup (discovered from the field value)
    }

    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID linkedTwinByTwinClassFieldId, UUID lookupTwinClassFieldId) throws ServiceException {
        var contextTwin = factoryItem.checkSingleContextTwin();
        twinService.loadFieldsValues(contextTwin);
        var fieldValue = contextTwin.getFieldValuesKit().get(linkedTwinByTwinClassFieldId);
        TwinEntity fromTwin = FieldValueLink.getSingleLinkedTwinSafe(fieldValue);
        return twinService.getTwinFieldValue(fromTwin, lookupTwinClassFieldId); // null when not found — batch entry turns it into an undefined value
    }
}
