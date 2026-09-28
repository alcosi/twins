package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;

import java.util.UUID;

@Component
public class FieldLookuperFromItemOutputLinkedTwinFields extends FieldLookuperLinkedTwinByField {

    @Override
    protected void beforeLookup(FactoryItemsBatch batch, UUID linkedTwinByTwinClassFieldId, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        if (!batch.getTwins().isEmpty())
            twinService.loadTwinFields(batch.getTwins()); // one bulk load for the navigation getFreshestValue; the linked twin lookup stays per item
    }

    /** Pass 1: the output twin's link field value carries the linked twin. */
    @Override
    protected TwinEntity linkedTwin(FactoryItem factoryItem, UUID linkedTwinByTwinClassFieldId) throws ServiceException {
        TwinEntity twinEntity = factoryItem.getTwin();
        FieldValue itemOutputField = getFreshestValue(twinEntity, linkedTwinByTwinClassFieldId, factoryItem.getFactoryContext());
        if (itemOutputField == null) // navigation field missing — the lookuper cannot even reach the linked twin
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + linkedTwinByTwinClassFieldId + "] is not present in output item fields");
        return FieldValueLink.getSingleLinkedTwinSafe(itemOutputField);
    }

    /** Pass 2: the lookup field from the preloaded linked twin. */
    @Override
    protected FieldValue lookupFieldValueOrNull(FactoryItem factoryItem, TwinEntity linkedTwin, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        return getFreshestValue(linkedTwin, lookupTwinClassField, factoryItem.getFactoryContext());
    }
}
