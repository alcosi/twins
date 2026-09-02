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
@Featurer(id = FeaturerTwins.ID_5601,
        name = "Date compare",
        description = "Compares this field's date value with another date field of the same twin class")
public class FieldValidatorDateCompare extends FieldValidator {
    @FeaturerParam(name = "Twin class field to compare with", description = "uuid of the other date twin class field", order = 1)
    public static final FeaturerParamUUID twinClassFieldIdToCompare = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldIdToCompare");

    @FeaturerParam(name = "Comparison operator", description = "ge (>=), gt (>), le (<=), lt (<), eq (=)", order = 2, optional = true, defaultValue = "ge")
    public static final FeaturerParamStringTwinsFieldValidatorCompareOperatorType compareOperator = new FeaturerParamStringTwinsFieldValidatorCompareOperatorType("compareOperator");

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, FieldValue value, Map<UUID, FieldValue> contextFields) throws ServiceException {
        UUID otherFieldId = twinClassFieldIdToCompare.extract(properties);
        FieldValidatorCompareOperator operator = compareOperator.extract(properties);
        FieldValue otherValue = resolveFieldValue(twinEntity, contextFields, otherFieldId);
        if (otherValue == null || otherValue.isEmpty())
            return ValidationResult.VALID; // the other field is not filled — there is nothing to compare yet
        if (!(value instanceof FieldValueDate thisDateValue) || !(otherValue instanceof FieldValueDate otherDateValue)) {
            log.error("{} or twinClassField[{}] is not a date field, date compare validator can not be applied",
                    value.getTwinClassField().logNormal(), otherFieldId);
            return new ValidationResult(false);
        }
        LocalDateTime thisDate = thisDateValue.getDate();
        LocalDateTime otherDate = otherDateValue.getDate();
        boolean isValid = switch (operator) {
            case ge -> !thisDate.isBefore(otherDate);
            case gt -> thisDate.isAfter(otherDate);
            case le -> !thisDate.isAfter(otherDate);
            case lt -> thisDate.isBefore(otherDate);
            case eq -> thisDate.isEqual(otherDate);
        };
        return isValid ? ValidationResult.VALID : new ValidationResult(false);
    }
}
