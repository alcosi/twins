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

import java.util.Collection;
import java.util.UUID;

@Component
public class FieldLookuperFromItemOutputLinkedTwinHeadTwinFields extends FieldLookuperLinkedTwinByField {

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

    /** The read source is the linked twin's HEAD — load the heads in bulk before the read. */
    @Override
    protected void preloadLinkedTwins(Collection<TwinEntity> linkedTwins) throws ServiceException {
        if (!linkedTwins.isEmpty())
            twinService.loadHead(linkedTwins); // one bulk head load for the whole batch
    }

    /** The head is the last reference hop of the navigation, resolved in memory after the bulk head load. */
    @Override
    protected TwinEntity readSource(TwinEntity linkedTwin) throws ServiceException {
        TwinEntity headTwin = linkedTwin.getHeadTwin();
        if (headTwin == null) // structural error — the linked twin has no head to look into, not a missing value
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "head twin of the item output linked twin can not be loaded, because head is null");
        return headTwin;
    }

    /** Pass 2: the lookup field from the preloaded head of the linked twin. */
    @Override
    protected FieldValue lookupFieldValueOrNull(FactoryItem factoryItem, TwinEntity readSource, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        return getFreshestValue(readSource, lookupTwinClassField, factoryItem.getFactoryContext());
    }
}
