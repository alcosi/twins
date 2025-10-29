package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
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
public class TwinValidatorTwinFieldNotNull extends TwinValidator {

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

        UUID nullFieldId = null;
        for (UUID fieldClassId : systemFieldIds) {
            Object value = SystemEntityService.getSystemFieldValue(twinEntity, fieldClassId);
            if (value == null) {
                nullFieldId = fieldClassId;
                break;
            }
        }

        if (nullFieldId == null && !dynamicFieldIds.isEmpty()) {
            twinService.loadFieldsValues(twinEntity);
            for (UUID fieldClassId : dynamicFieldIds) {
                FieldValue fieldValue = twinEntity.getFieldValuesKit().get(fieldClassId);
                if (fieldValue == null || fieldValue.isEmpty()) {
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
}
