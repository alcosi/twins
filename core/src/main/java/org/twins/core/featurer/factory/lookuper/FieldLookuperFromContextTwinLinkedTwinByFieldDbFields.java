package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.List;
import java.util.UUID;

@Component
public class FieldLookuperFromContextTwinLinkedTwinByFieldDbFields extends FieldLookuperNavigated implements FieldLookuperLinked {

    @Override
    public LookupResult lookupFieldValue(FactoryItemsBatch factoryItemsBatch, UUID linkedTwinByTwinClassFieldId, UUID lookupTwinClassFieldId) throws ServiceException {
        return navigate(factoryItemsBatch, lookupTwinClassFieldId, List.of(NavigationStage.valuesLinkField(linkedTwinByTwinClassFieldId)));
    }

    @Override
    protected TwinEntity startTwin(FactoryItem factoryItem) throws ServiceException {
        return factoryItem.checkSingleContextTwin();
    }

    @Override
    protected FieldValue read(FactoryItem factoryItem, TwinEntity readSource, TwinClassFieldEntity lookupTwinClassField) throws ServiceException {
        return twinService.getTwinFieldValue(readSource, lookupTwinClassField); // the linked twin's db field
    }
}
