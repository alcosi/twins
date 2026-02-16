package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
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
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
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

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        fill(properties, factoryItem, templateTwin, fieldLookupers.getFromContextTwinDbFields());
    }

    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin, FieldLookuperNearest fieldLookuperNearest) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        UUID fieldId = FillerBasicsFieldFromTwinField.fieldId.extract(properties);
        FieldValue fieldValue = fieldLookuperNearest.lookupFieldValue(factoryItem, fieldId);
        String fieldName;
        switch (fieldValue) {
            case FieldValueText fieldValueText -> fieldName = handleTextField(fieldId, fieldValueText, outputTwinEntity);
            case FieldValueUser fieldValueUser -> fieldName = handleUserField(fieldValueUser, outputTwinEntity, fieldValue);
            default -> throw new ServiceException(
                    ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE,
                    fieldValue.getTwinClassField().logShort() + " is incorrect field type"
            );
        }
        log.info("{} with field[{}] will be filled from context {}", outputTwinEntity.logShort(), fieldName, fieldValue.getTwinClassField().logShort()
        );
    }

    private String handleTextField(UUID fieldId, FieldValueText fieldValueText, TwinEntity outputTwinEntity) {
        if (fieldId.equals(SystemEntityService.TWIN_CLASS_FIELD_TWIN_NAME)) {
            outputTwinEntity.setName(fieldValueText.getValue());
            return TwinEntity.Fields.name;
        } else if (fieldId.equals(SystemEntityService.TWIN_CLASS_FIELD_TWIN_DESCRIPTION)) {
            outputTwinEntity.setDescription(fieldValueText.getValue());
            return TwinEntity.Fields.description;
        }
        return null;
    }

    private String handleUserField(FieldValueUser fieldValueUser, TwinEntity outputTwinEntity, FieldValue fieldValue) throws ServiceException {
        if (fieldValueUser.isEmpty()) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, fieldValue.getTwinClassField().logShort() + " is not filled");
        } else if (fieldValueUser.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, fieldValue.getTwinClassField().logShort() + " is filled by multiple users");
        } else {
            UserEntity user = fieldValueUser.getItems().getFirst();
            if (fieldId.equals(SystemEntityService.TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER)) {
                outputTwinEntity
                        .setAssignerUser(user)
                        .setAssignerUserId(user.getId());
                return TwinEntity.Fields.assignerUserId;
            } else if (fieldId.equals(SystemEntityService.TWIN_CLASS_FIELD_TWIN_CREATOR_USER)) {
                outputTwinEntity
                        .setCreatedByUser(user)
                        .setCreatedByUserId(user.getId());
                return TwinEntity.Fields.createdByUserId;
            }
        }
        return null;
    }
}
