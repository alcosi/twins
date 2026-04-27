package org.twins.core.featurer.twin.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueBoolean;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1621,
        name = "Twin boolean field has value",
        description = "Validates that twin has a specific value in the specified boolean field")
@RequiredArgsConstructor
public class TwinValidatorTwinFieldBooleanHasValue extends TwinValidator {
    @FeaturerParam(name = "Twin class field id", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "Value", order = 2)
    public static final FeaturerParamBoolean value = new FeaturerParamBoolean("value");

    private final TwinService twinService;

    @Override
    protected CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        UUID fieldId = twinClassFieldId.extract(properties);
        boolean expectedValue = value.extract(properties);

        twinService.loadFieldsValues(twinEntityCollection);

        CollectionValidationResult collectionValidationResult = new CollectionValidationResult();
        for (var twinEntity : twinEntityCollection) {
            FieldValue fieldValue = twinEntity.getFieldValuesKit().get(fieldId);

            boolean isValid;
            if (fieldValue == null || fieldValue.isEmpty()) {
                isValid = false;
            } else if (!(fieldValue instanceof FieldValueBoolean fvb)) {
                isValid = false;
            } else {
                isValid = fvb.getValue() == expectedValue;
            }

            ValidationResult result = buildResult(
                    isValid,
                    invert,
                    twinEntity.logShort() + " field[" + fieldId + "] is not [" + expectedValue + "]",
                    twinEntity.logShort() + " field[" + fieldId + "] is [" + expectedValue + "]");
            collectionValidationResult.getTwinsResults().put(twinEntity.getId(), result);
        }
        return collectionValidationResult;
    }
}
