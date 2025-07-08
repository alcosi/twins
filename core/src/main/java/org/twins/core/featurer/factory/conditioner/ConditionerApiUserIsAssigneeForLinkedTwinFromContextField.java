package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.TwinLinkService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2435,
        name = "Is current user assignee for linked twin form context field",
        description = "")
@Slf4j
public class ConditionerApiUserIsAssigneeForLinkedTwinFromContextField extends Conditioner {

    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");
    @Lazy
    @Autowired
    AuthService authService;
    @Lazy
    @Autowired
    TwinLinkService twinLinkService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        UUID extractedTwinClassFieldId = twinClassFieldId.extract(properties);
        FieldValue fieldValue = factoryItem.getOutput().getFields().get(extractedTwinClassFieldId);
        if (!(fieldValue instanceof FieldValueLink itemOutputFieldLink))
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + extractedTwinClassFieldId + "] is not of type link");
        if (itemOutputFieldLink.getTwinLinks().size() > 1)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + extractedTwinClassFieldId + "] has " + itemOutputFieldLink.getTwinLinks().size() +  " linked twins in twinId[" + factoryItem.getOutput().getTwinId() + "]");
        TwinEntity linkDstTwin = twinLinkService.getDstTwinSafe(itemOutputFieldLink.getTwinLinks().getFirst());
        return linkDstTwin.getAssignerUserId().equals(authService.getApiUser().getUserId());
    }
}
