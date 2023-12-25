package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 2315,
        name = "FillerBasicsAssigneeFromContextField",
        description = "")
@Slf4j
public class FillerBasicsAssigneeFromContextField extends Filler {
    @FeaturerParam(name = "assigneeField", description = "")
    public static final FeaturerParamUUID assigneeField = new FeaturerParamUUID("assigneeField");

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinStatusService twinStatusService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutputTwin().getTwinEntity();
        UUID assigneeFieldId = assigneeField.extract(properties);
        FieldValue assigneeField = factoryItem.getFactoryContext().getFields().get(assigneeFieldId);
        if (assigneeField == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + assigneeFieldId + "] is not present in context ");
        if (assigneeField instanceof FieldValueUser fieldValueUser) {
            if (CollectionUtils.isEmpty(fieldValueUser.getUsers()))
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, assigneeField.getTwinClassField().logShort() + " is not filled");
            else if (fieldValueUser.getUsers().size() > 1) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, assigneeField.getTwinClassField().logShort() + " is filled by multiply users");
            } else {
                log.info(outputTwinEntity.logShort() + " [assignee] will be filled from context " + assigneeField.getTwinClassField().logShort());
                UserEntity assignee = fieldValueUser.getUsers().get(0);
                outputTwinEntity
                        .setAssignerUser(assignee)
                        .setAssignerUserId(assignee.getId());
            }
        } else
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, assigneeField.getTwinClassField().logShort() + " has unexpected type");
    }
}
