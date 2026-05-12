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
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.featurer.fieldtyper.value.FieldValueUserSingle;
import org.twins.core.featurer.params.FeaturerParamBasicsTwinBasicField;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.SystemEntityService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2344,
        name = "Basic field from twin field",
        description = "")
@Slf4j
public class FillerBasicsFieldFromTwinField extends Filler {
    @FeaturerParam(name = "Field id", description = "", order = 1)
    public static final FeaturerParamUUID fieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("fieldId");

    @FeaturerParam(name = "Destination basics user field", description = "", order = 2, optional = true)
    public static final FeaturerParamBasicsTwinBasicField dstBasicsUser = new FeaturerParamBasicsTwinBasicField("dstBasicsUser");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        fill(properties, factoryItem, templateTwin, fieldLookupers.getFromContextTwinDbFields());
    }

    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin, FieldLookuperNearest fieldLookuperNearest) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        UUID sourceFieldId = fieldId.extract(properties);
        TwinBasicFields.Basics dstUserBasic = dstBasicsUser.extract(properties);
        FieldValue fieldValue = fieldLookuperNearest.lookupFieldValue(factoryItem, sourceFieldId);
        if (fieldValue == null) {
            throw new ServiceException(
                    ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED,
                    "Field value for fieldId[" + sourceFieldId + "] is not found"
            );
        }
        String fieldName;
        switch (fieldValue) {
            case FieldValueText fieldValueText -> fieldName = handleTextField(sourceFieldId, fieldValueText, outputTwinEntity);
            case FieldValueUser fieldValueUser -> fieldName = handleUserField(fieldValueUser, outputTwinEntity, fieldValue, sourceFieldId, dstUserBasic);
            case FieldValueUserSingle fieldValueUserSingle -> fieldName = handleUserFieldSingle(fieldValueUserSingle, outputTwinEntity, fieldValue, sourceFieldId, dstUserBasic);
            default -> throw new ServiceException(
                    ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE,
                    fieldValue.getTwinClassField().logShort() + " is incorrect field type"
            );
        }
        log.info("{} with field[{}] will be filled from context {}", outputTwinEntity.logShort(), fieldName, fieldValue.getTwinClassField().logShort()
        );
    }

    private String handleTextField(UUID fieldId, FieldValueText fieldValueText, TwinEntity outputTwinEntity) throws ServiceException {
        if (fieldValueText.getValue() == null || fieldValueText.getValue().isBlank()) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, fieldValueText.getTwinClassField().logShort() + " is not filled");
        }
        if (fieldId.equals(SystemEntityService.TWIN_CLASS_FIELD_TWIN_NAME)) {
            outputTwinEntity.setName(fieldValueText.getValue());
            return TwinEntity.Fields.name;
        } else if (fieldId.equals(SystemEntityService.TWIN_CLASS_FIELD_TWIN_DESCRIPTION)) {
            outputTwinEntity.setDescription(fieldValueText.getValue());
            return TwinEntity.Fields.description;
        }
        throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Text field " + fieldValueText.getTwinClassField().logShort() + ": set fieldId to system name/description field id, or use user field mapping for user basics");
    }

    private String handleUserField(FieldValueUser fieldValueUser, TwinEntity outputTwinEntity, FieldValue fieldValue, UUID sourceFieldId, TwinBasicFields.Basics dstUserBasic) throws ServiceException {
        if (fieldValueUser.isEmpty()) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, fieldValue.getTwinClassField().logShort() + " is not filled");
        } else if (fieldValueUser.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, fieldValue.getTwinClassField().logShort() + " is filled by multiple users");
        } else {
            UserEntity user = fieldValueUser.getItems().getFirst();
            return mapUserToOutputTwinBasics(user, outputTwinEntity, fieldValue, sourceFieldId, dstUserBasic);
        }
    }

    /**
     * Base assignee / creator / owner fields use {@link org.twins.core.featurer.fieldtyper.FieldTyperBaseUserField} and deserialize as {@link FieldValueUserSingle}.
     */
    private String handleUserFieldSingle(FieldValueUserSingle fieldValueUserSingle, TwinEntity outputTwinEntity, FieldValue fieldValue, UUID sourceFieldId, TwinBasicFields.Basics dstUserBasic) throws ServiceException {
        if (fieldValueUserSingle.isEmpty() || fieldValueUserSingle.getValue() == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, fieldValue.getTwinClassField().logShort() + " is not filled");
        }
        return mapUserToOutputTwinBasics(fieldValueUserSingle.getValue(), outputTwinEntity, fieldValue, sourceFieldId, dstUserBasic);
    }

    private String mapUserToOutputTwinBasics(UserEntity user, TwinEntity outputTwinEntity, FieldValue fieldValue, UUID sourceFieldId, TwinBasicFields.Basics dstUserBasic) throws ServiceException {
        if (user == null || user.getId() == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, fieldValue.getTwinClassField().logShort() + " contains empty user");
        }
        if (dstUserBasic != null) {
            return applyUserToOutputBasics(outputTwinEntity, user, dstUserBasic);
        }
        if (sourceFieldId.equals(SystemEntityService.TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER)) {
            outputTwinEntity
                    .setAssignerUser(user)
                    .setAssignerUserId(user.getId());
            return TwinEntity.Fields.assignerUserId;
        } else if (sourceFieldId.equals(SystemEntityService.TWIN_CLASS_FIELD_TWIN_CREATOR_USER)) {
            outputTwinEntity
                    .setCreatedByUser(user)
                    .setCreatedByUserId(user.getId());
            return TwinEntity.Fields.createdByUserId;
        }
        throw new ServiceException(
                ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR,
                "User field " + fieldValue.getTwinClassField().logShort()
                        + ": set dstBasicsUser to assigneeUserId or createdByUserId, or use system assignee/creator field id as fieldId"
        );
    }

    private static String applyUserToOutputBasics(TwinEntity outputTwinEntity, UserEntity user, TwinBasicFields.Basics dstUserBasic) throws ServiceException {
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
            case name, description -> throw new ServiceException(
                    ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR,
                    "dstBasicsUser for user fields must be assigneeUserId or createdByUserId, got: " + dstUserBasic
            );
        };
    }
}
