package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.twin.TwinService;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1614,
        name = "Twin fields value is not null",
        description = "")
public class TwinValidatorTwinFieldNotNull extends TwinValidator{

    @FeaturerParam(name = "Twin class field ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet twinClassFieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("twinClassFieldIds");

    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        Set<UUID> fieldClassIds = twinClassFieldIds.extract(properties);

        Set<UUID> systemFieldIds = new HashSet<>();
        Set<UUID> dynamicFieldIds = new HashSet<>();

        for (UUID fieldClassId : fieldClassIds) {
            if (SystemEntityService.isSystemField(fieldClassId)) {
                systemFieldIds.add(fieldClassId);
            } else {
                dynamicFieldIds.add(fieldClassId);
            }
        }

        twinService.loadFieldsValues(twinEntity);

        UUID nullFieldId = null;
        for (UUID fieldClassId : dynamicFieldIds) {
            FieldValue fieldValue = twinEntity.getFieldValuesKit().get(fieldClassId);
            if (fieldValue == null || fieldValue.isEmpty()) {
                nullFieldId = fieldClassId;
                break;
            }
        }

        if (nullFieldId == null) {
            for (UUID fieldClassId : systemFieldIds) {
                String value = getSystemFieldValue(twinEntity, fieldClassId);
                if (value == null || value.isEmpty()) {
                    nullFieldId = fieldClassId;
                    break;
                }
            }
        }

        boolean isValid = (nullFieldId == null);
        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " field of class[" + nullFieldId + "] is null",
                twinEntity.logShort() + " there are no null fields found");
    }

    public String getSystemFieldValue(TwinEntity twinEntity, UUID systemFieldId) throws ServiceException {
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_NAME.equals(systemFieldId)) {
            return twinEntity.getName();
        }
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_DESCRIPTION.equals(systemFieldId)) {
            return twinEntity.getDescription();
        }
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID.equals(systemFieldId)) {
            return twinEntity.getExternalId();
        }
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_OWNER_USER.equals(systemFieldId)) {
            return twinEntity.getOwnerUserId().toString();
        }
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER.equals(systemFieldId)) {
            return twinEntity.getAssignerUserId().toString();
        }
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_CREATOR_USER.equals(systemFieldId)) {
            return twinEntity.getCreatedByUserId().toString();
        }
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_HEAD.equals(systemFieldId)) {
            return twinEntity.getHeadTwinId().toString();
        }
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_STATUS.equals(systemFieldId)) {
            return twinEntity.getTwinStatusId().toString();
        }
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_CREATED_AT.equals(systemFieldId)) {
            return twinEntity.getCreatedAt().toString();
        }
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_ID.equals(systemFieldId)) {
            return twinEntity.getId().toString();
        }
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_TWIN_CLASS_ID.equals(systemFieldId)) {
            return twinEntity.getTwinClassId().toString();
        }
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_ALIASES.equals(systemFieldId)) {
            return twinEntity.getTwinAliases().toString();
        }
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_TAGS.equals(systemFieldId)) {
            return twinEntity.getTags().toString();
        }
        if (SystemEntityService.TWIN_CLASS_FIELD_TWIN_MARKERS.equals(systemFieldId)) {
            return twinEntity.getMarkers().toString();
        }
        return null;
    }
}
