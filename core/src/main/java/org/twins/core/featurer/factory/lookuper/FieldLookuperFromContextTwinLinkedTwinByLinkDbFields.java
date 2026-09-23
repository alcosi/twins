package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FieldLookuperFromContextTwinLinkedTwinByLinkDbFields extends FieldLookuperLinkedTwinByLink {

    @Override
    protected void beforeLookup(FactoryItemsBatch batch, UUID linkedTwinByLinkId, UUID lookupTwinClassFieldId) throws ServiceException {
        if (batch.getContextTwins().isEmpty())
            return;
        twinLinkService.loadTwinLinks(batch.getContextTwins()); // one bulk load of the links
        List<TwinLinkEntity> matchedLinks = new ArrayList<>();
        for (TwinEntity contextTwin : batch.getContextTwins()) {
            // getGrouped returns an empty list for a missing linkId — per-item lookup keeps its own error reporting
            matchedLinks.addAll(contextTwin.getTwinLinks().getForwardLinks().getGrouped(linkedTwinByLinkId));
        }
        if (matchedLinks.isEmpty())
            return;
        twinLinkService.loadDstTwin(matchedLinks); // one bulk load of the matched dst twins
        List<TwinEntity> dstTwins = new ArrayList<>(matchedLinks.size());
        for (TwinLinkEntity matchedLink : matchedLinks) {
            if (matchedLink.getDstTwin() != null)
                dstTwins.add(matchedLink.getDstTwin());
        }
        if (!dstTwins.isEmpty())
            twinService.loadTwinFields(dstTwins); // one bulk load of the dst twin fields
    }

    @Override
    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID linkedTwinByLinkId, UUID lookupTwinClassFieldId) throws ServiceException {
        var contextTwin = factoryItem.checkSingleContextTwin();
        twinLinkService.loadTwinLinks(contextTwin);
        TwinEntity fromTwin;
        try {
            var links = contextTwin.getTwinLinks().getForwardLinks().getGrouped(linkedTwinByLinkId);
            twinLinkService.loadDstTwin(links);
            fromTwin = links.getFirst().getDstTwin();
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + lookupTwinClassFieldId + "] is not present in context twin linked twins fields");
        }
        FieldValue fieldValue = twinService.getTwinFieldValue(fromTwin, lookupTwinClassFieldId);
        if (fieldValue == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + lookupTwinClassFieldId + "] is not present in head twin fields");
        return fieldValue;
    }
}
