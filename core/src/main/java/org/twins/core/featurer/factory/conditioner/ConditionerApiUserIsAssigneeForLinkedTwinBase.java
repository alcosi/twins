package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.TwinLinkService;

import java.util.UUID;

@Slf4j
public abstract class ConditionerApiUserIsAssigneeForLinkedTwinBase extends Conditioner {
    @Lazy
    @Autowired
    AuthService authService;
    @Lazy
    @Autowired
    TwinLinkService twinLinkService;

    protected boolean check(FactoryItem factoryItem, FieldValue fieldValue, UUID extractedTwinClassFieldId) throws ServiceException {
        if (!(fieldValue instanceof FieldValueLink itemOutputFieldLink))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + extractedTwinClassFieldId + "] is not of type link");
        if (itemOutputFieldLink.getTwinLinks().size() > 1)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + extractedTwinClassFieldId + "] has " + itemOutputFieldLink.getTwinLinks().size() + " linked twins in twinId[" + factoryItem.getOutput().getTwinId() + "]");
        TwinEntity linkDstTwin = twinLinkService.getDstTwinSafe(itemOutputFieldLink.getTwinLinks().getFirst());
        return linkDstTwin.getAssignerUserId().equals(authService.getApiUser().getUserId());
    }
}
