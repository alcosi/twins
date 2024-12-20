package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2324,
        name = "FillerBasicsAssigneeFromContext",
        description = "")
@Slf4j
public class FillerBasicsAssigneeFromContext extends Filler {
    @FeaturerParam(name = "assigneeField", description = "")
    public static final FeaturerParamUUID assigneeField = new FeaturerParamUUIDTwinsTwinClassFieldId("assigneeField");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        fill(properties, factoryItem, templateTwin, FieldLookupMode.fromContextFieldsAndContextTwinDbFields);
    }

    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin, FieldLookupMode fieldLookupMode) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        TwinEntity contextTwin = factoryItem.checkSingleContextTwin();
        UUID assigneeFieldId = assigneeField.extract(properties);
        FieldValue fieldValue = factoryService.lookupFieldValue(factoryItem, assigneeFieldId, fieldLookupMode);
        if (fieldValue instanceof FieldValueUser fieldValueUser) {
            if (CollectionUtils.isEmpty(fieldValueUser.getUsers()))
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, fieldValue.getTwinClassField().logShort() + " is not filled");
            else if (fieldValueUser.getUsers().size() > 1) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, fieldValue.getTwinClassField().logShort() + " is filled by multiply users");
            } else {
                log.info(outputTwinEntity.logShort() + " [assignee] will be filled from context " + fieldValue.getTwinClassField().logShort());
                UserEntity assignee = fieldValueUser.getUsers().get(0);
                outputTwinEntity
                        .setAssignerUser(assignee)
                        .setAssignerUserId(assignee.getId());
            }
        } else
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, fieldValue.getTwinClassField().logShort() + " is not for user");
    }
}
