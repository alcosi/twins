package org.twins.core.featurer.fieldvalidator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.enums.twinclass.FieldValidatorCompareOperator;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.params.FeaturerParamStringTwinsFieldValidatorCompareOperatorType;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_5603,
        name = "Date compare with parent",
        description = "Compares this field's date value with a date field of the head (parent) twin")
public class FieldValidatorDateCompareWithParent extends FieldValidator {
    @FeaturerParam(name = "Parent twin class field to compare with", description = "uuid of the date twin class field on the head twin", order = 1)
    public static final FeaturerParamUUID parentTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("parentTwinClassFieldId");

    @FeaturerParam(name = "Comparison operator", description = "ge (>=), gt (>), le (<=), lt (<), eq (=)", order = 2, optional = true, defaultValue = "lt")
    public static final FeaturerParamStringTwinsFieldValidatorCompareOperatorType compareOperator = new FeaturerParamStringTwinsFieldValidatorCompareOperatorType("compareOperator");

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, FieldValue value, Map<UUID, FieldValue> contextFields) throws ServiceException {
        UUID parentFieldId = parentTwinClassFieldId.extract(properties);
        FieldValidatorCompareOperator operator = compareOperator.extract(properties);

        TwinEntity headTwin = twinService.loadHead(twinEntity);
        if (headTwin == null)
            return ValidationResult.VALID; // no parent — nothing to compare

        twinService.loadFieldsValues(headTwin);
        FieldValue parentValue = headTwin.getFieldValuesKit() == null ? null : headTwin.getFieldValuesKit().get(parentFieldId);
        if (parentValue == null || parentValue.isEmpty())
            return ValidationResult.VALID; // parent field is not filled — skip

        if (!(value instanceof FieldValueDate thisDateValue) || !(parentValue instanceof FieldValueDate parentDateValue)) {
            log.error("{} or parent twinClassField[{}] is not a date field, date compare with parent can not be applied",
                    value.getTwinClassField().logNormal(), parentFieldId);
            return new ValidationResult(false);
        }

        LocalDateTime thisDate = thisDateValue.getDate();
        LocalDateTime parentDate = parentDateValue.getDate();
        boolean isValid = switch (operator) {
            case ge -> !thisDate.isBefore(parentDate);
            case gt -> thisDate.isAfter(parentDate);
            case le -> !thisDate.isAfter(parentDate);
            case lt -> thisDate.isBefore(parentDate);
            case eq -> thisDate.isEqual(parentDate);
        };
        return isValid ? ValidationResult.VALID : new ValidationResult(false);
    }
}
