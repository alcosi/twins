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
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.featurer.params.FeaturerParamStringTwinsFactoryFieldLookuper;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2344,
        name = "Basic field from twin field",
        description = "")
@Slf4j
public class FillerBasicsFieldFromTwinField extends FillerFieldLookup {
    @FeaturerParam(name = "Field id", description = "", order = 1)
    public static final FeaturerParamUUID fieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("fieldId");

    @FeaturerParam(name = "Field lookuper", description = "Source of the field value", order = 99, optional = true, defaultValue = "fromContextTwinDbFields")
    public static final FeaturerParamStringTwinsFactoryFieldLookuper fieldLookuperParam = new FeaturerParamStringTwinsFactoryFieldLookuper("fieldLookuper");

    @Override
    protected FieldLookuperNearest lookuper(Properties properties) {
        return (FieldLookuperNearest) fieldLookupers.getByType(fieldLookuperParam.extract(properties));
    }

    @Override
    protected UUID lookupFieldId(Properties properties) throws ServiceException {
        return fieldId.extract(properties);
    }

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin, FieldValue fieldValue) throws ServiceException {
        fieldValue.assertIsDefined(fieldValue.getTwinClassField().logNormal() + " is not present in context twin db fields");
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        String fieldName;
        switch (fieldValue) {
            case FieldValueText fieldValueText -> fieldName = handleTextField(fieldValueText, outputTwinEntity);
            case FieldValueUser fieldValueUser -> fieldName = handleUserField(fieldValueUser, outputTwinEntity);
            default -> throw new ServiceException(
                    ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE,
                    fieldValue.getTwinClassField().logShort() + " is incorrect field type"
            );
        }
        log.info("{} with field[{}] will be filled from context {}", outputTwinEntity.logShort(), fieldName, fieldValue.getTwinClassField().logShort()
        );
    }

    private String handleTextField(FieldValueText fieldValueText, TwinEntity outputTwinEntity) {
        if (fieldValueText.getTwinClassFieldId().equals(SystemIds.TwinClassField.Base.NAME)) {
            outputTwinEntity.setName(fieldValueText.getValue());
            return TwinEntity.Fields.name;
        } else if (fieldValueText.getTwinClassFieldId().equals(SystemIds.TwinClassField.Base.DESCRIPTION)) {
            outputTwinEntity.setDescription(fieldValueText.getValue());
            return TwinEntity.Fields.description;
        }
        return null;
    }

    private String handleUserField(FieldValueUser fieldValueUser, TwinEntity outputTwinEntity) throws ServiceException {
        if (fieldValueUser.isEmpty()) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, fieldValueUser.getTwinClassField().logShort() + " is not filled");
        } else if (fieldValueUser.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, fieldValueUser.getTwinClassField().logShort() + " is filled by multiple users");
        } else {
            UserEntity user = fieldValueUser.getItems().getFirst();
            if (fieldValueUser.getTwinClassFieldId().equals(SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID)) {
                outputTwinEntity
                        .setAssignerUser(user)
                        .setAssignerUserId(user.getId());
                return TwinEntity.Fields.assignerUserId;
            } else if (fieldValueUser.getTwinClassFieldId().equals(SystemIds.TwinClassField.Base.CREATOR_USER_ID)) {
                outputTwinEntity
                        .setCreatedByUser(user)
                        .setCreatedByUserId(user.getId());
                return TwinEntity.Fields.createdByUserId;
            }
        }
        return null;
    }
}
