package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinBasicFields;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.featurer.fieldtyper.value.FieldValueUserSingle;
import org.twins.core.featurer.params.FeaturerParamBasicsTwinBasicField;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2353,
        name = "Basic user field from twin field",
        description = "Maps a custom user field to output twin assignee/creator basics via dstBasicsUserFiledName")
@Slf4j
public class FillerBasicsFieldUserFromTwinField extends Filler {
    @FeaturerParam(name = "Field id", description = "", order = 1)
    public static final FeaturerParamUUID fieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("fieldId");

    @FeaturerParam(name = "Destination basics user field name", description = "assigneeUserId or createdByUserId", order = 2)
    public static final FeaturerParamBasicsTwinBasicField dstBasicsUserFiledName = new FeaturerParamBasicsTwinBasicField("dstBasicsUserFiledName");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        UUID sourceFieldId = fieldId.extract(properties);
        TwinBasicFields.Basics dstUserBasic = dstBasicsUserFiledName.extract(properties);
        FieldValue fieldValue = fieldLookupers.getFromContextTwinDbFields().lookupFieldValue(factoryItem, sourceFieldId);
        if (fieldValue == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, "Field value for fieldId[" + sourceFieldId + "] is not found");
        }
        UserEntity user = extractSingleUser(fieldValue);
        String fieldName = applyUserToOutputBasics(outputTwinEntity, user, dstUserBasic);
        log.info("{} with field[{}] will be filled from context {}", outputTwinEntity.logShort(), fieldName, fieldValue.getTwinClassField().logShort());
    }

    private static UserEntity extractSingleUser(FieldValue fieldValue) throws ServiceException {
        if (fieldValue instanceof FieldValueUser fieldValueUser) {
            if (fieldValueUser.isEmpty()) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, fieldValue.getTwinClassField().logShort() + " is not filled");
            }
            if (fieldValueUser.size() > 1) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, fieldValue.getTwinClassField().logShort() + " is filled by multiple users");
            }
            return fieldValueUser.getItems().getFirst();
        }
        if (fieldValue instanceof FieldValueUserSingle fieldValueUserSingle) {
            if (fieldValueUserSingle.isEmpty() || fieldValueUserSingle.getValue() == null) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, fieldValue.getTwinClassField().logShort() + " is not filled");
            }
            return fieldValueUserSingle.getValue();
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, fieldValue.getTwinClassField().logShort() + " is not a user field");
    }

    private static String applyUserToOutputBasics(TwinEntity outputTwinEntity, UserEntity user, TwinBasicFields.Basics dstUserBasic) throws ServiceException {
        if (user == null || user.getId() == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, "user field contains empty user");
        }
        return switch (dstUserBasic) {
            case assigneeUserId -> {
                outputTwinEntity
                        .setAssignerUser(user)
                        .setAssignerUserId(user.getId());
                yield TwinEntity.Fields.assignerUserId;
            }
            case createdByUserId -> {
                outputTwinEntity
                        .setCreatedByUser(user)
                        .setCreatedByUserId(user.getId());
                yield TwinEntity.Fields.createdByUserId;
            }
            default -> throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "dstBasicsUserFiledName must be assigneeUserId or createdByUserId, got: " + dstUserBasic);
        };
    }
}
