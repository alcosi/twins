package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2322,
        name = "Basics assignee from context field twin assignee",
        description = "If value of context field is an id of other twin (link) we will get assignee from that twin")
@Slf4j
public class FillerBasicsAssigneeFromContextFieldTwinAssignee extends Filler {
    @FeaturerParam(name = "Link field", description = "", order = 1)
    public static final FeaturerParamUUID linkField = new FeaturerParamUUIDTwinsTwinClassFieldId("linkField");

    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID assigneeFieldId = linkField.extract(properties);
        FieldValue assigneeField = factoryItem.getFactoryContext().getFields().get(assigneeFieldId);
        fill(properties, factoryItem, templateTwin, assigneeField, assigneeFieldId);
    }

    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin, FieldValue assigneeField, UUID assigneeFieldId) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        if (assigneeField == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + assigneeFieldId + "] is not present in context ");
        if (assigneeField instanceof FieldValueLink fieldValueLink) {
            if (fieldValueLink.isEmpty())
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, assigneeField.getTwinClassField().logShort() + " is not filled");
            else if (fieldValueLink.size() > 1) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, assigneeField.getTwinClassField().logShort() + " is filled by multiply twins");
            } else {
                log.info(outputTwinEntity.logShort() + " [assignee] will be filled from twin " + fieldValueLink.getItems());
                TwinLinkEntity linkEntity = fieldValueLink.getItems().getFirst();
                UserEntity assignee = twinService.getTwinAssignee(linkEntity.getDstTwinId());
                if (assignee == null)
                    throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No assignee for twin[" + linkEntity.getDstTwinId() + "]");
                outputTwinEntity
                        .setAssignerUser(assignee)
                        .setAssignerUserId(assignee.getId());
            }
        } else
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, assigneeField.getTwinClassField().logShort() + " has unexpected type");
    }
}
