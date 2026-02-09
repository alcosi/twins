package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.UUID;

@Component
public class FieldLookuperFromContextTwinLinkedTwinByLinkDbFields extends FieldLookuperLinkedTwinByLink {


    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID linkedTwinByLinkId, UUID lookupTwinClassFieldId) throws ServiceException {
        var contextTwin = factoryItem.checkSingleContextTwin();
        twinLinkService.loadTwinLinks(contextTwin);
        TwinEntity fromTwin;
        try {
            fromTwin = contextTwin.getTwinLinks().getForwardLinks().getGrouped(linkedTwinByLinkId).getFirst().getDstTwin();
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + lookupTwinClassFieldId + "] is not present in context twin linked twins fields");
        }
        FieldValue fieldValue = twinService.getTwinFieldValue(fromTwin, lookupTwinClassFieldId);
        if (fieldValue == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + lookupTwinClassFieldId + "] is not present in head twin fields");
        return fieldValue;
    }
}
