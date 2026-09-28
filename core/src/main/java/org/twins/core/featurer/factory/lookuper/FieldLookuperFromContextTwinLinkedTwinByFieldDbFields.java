package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;

import java.util.UUID;

@Component
public class FieldLookuperFromContextTwinLinkedTwinByFieldDbFields extends FieldLookuperLinkedTwinByField {

    @Override
    protected void beforeLookup(FactoryItemsBatch batch, UUID linkedTwinByTwinClassFieldId, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        if (!batch.getContextTwins().isEmpty())
            twinService.loadFieldsValues(batch.getContextTwins()); // one bulk load for the whole batch; the linked twin itself stays a per-item navigation (discovered from the field value)
    }

    /** Pass 1: the context twin's link field value carries the linked twin. */
    @Override
    protected TwinEntity linkedTwin(FactoryItem factoryItem, UUID linkedTwinByTwinClassFieldId) throws ServiceException {
        var contextTwin = factoryItem.checkSingleContextTwin();
        var fieldValue = contextTwin.getFieldValuesKit().get(linkedTwinByTwinClassFieldId);
        return FieldValueLink.getSingleLinkedTwinSafe(fieldValue);
    }

    /** Pass 2: the lookup field from the preloaded linked twin. */
    @Override
    protected FieldValue lookupFieldValueOrNull(FactoryItem factoryItem, TwinEntity linkedTwin, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        return twinService.getTwinFieldValue(linkedTwin, lookupTwinClassField);
    }
}
