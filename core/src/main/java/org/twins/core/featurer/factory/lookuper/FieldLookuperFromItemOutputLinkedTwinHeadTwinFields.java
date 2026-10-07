package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;

import java.util.List;
import java.util.UUID;

@Component
public class FieldLookuperFromItemOutputLinkedTwinHeadTwinFields extends FieldLookuperNavigated implements FieldLookuperLinked {

    @Override
    public LookupResult lookupFieldValue(FactoryItemsBatch factoryItemsBatch, UUID linkedTwinByTwinClassFieldId, UUID lookupTwinClassFieldId) throws ServiceException {
        return navigate(factoryItemsBatch, lookupTwinClassFieldId, List.of(NavigationStage.linkField(linkedTwinByTwinClassFieldId), NavigationStage.head()));
    }

    @Override
    protected TwinEntity startTwin(FactoryItem factoryItem) throws ServiceException {
        return factoryItem.getTwin();
    }
    // read = default: the freshest value of the lookup field on the linked twin's head
}
