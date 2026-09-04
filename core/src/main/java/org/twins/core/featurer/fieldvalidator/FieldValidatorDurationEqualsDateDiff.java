package org.twins.core.featurer.fieldvalidator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_5602,
        name = "Duration equals date diff",
        description = "Checks that a duration field equals (end date - start date) in whole days when all three values are filled")
public class FieldValidatorDurationEqualsDateDiff extends FieldValidator {
    @FeaturerParam(name = "Start date field", description = "uuid of the start date twin class field", order = 1)
    public static final FeaturerParamUUID startDateTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("startDateTwinClassFieldId");

    @FeaturerParam(name = "End date field", description = "uuid of the end date twin class field", order = 2)
    public static final FeaturerParamUUID endDateTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("endDateTwinClassFieldId");

    @FeaturerParam(name = "Duration field", description = "uuid of the duration twin class field; defaults to the field being validated", order = 3, optional = true)
    public static final FeaturerParamUUID durationTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("durationTwinClassFieldId");

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, FieldValue value, Map<UUID, FieldValue> contextFields) throws ServiceException {
        UUID startFieldId = startDateTwinClassFieldId.extract(properties);
        UUID endFieldId = endDateTwinClassFieldId.extract(properties);
        UUID durationFieldId = durationTwinClassFieldId.extract(properties);
        if (durationFieldId == null)
            durationFieldId = value.getTwinClassField().getId();

        FieldValue durationValue = resolveValidatedOrContextValue(twinEntity, value, contextFields, durationFieldId);
        FieldValue startValue = resolveValidatedOrContextValue(twinEntity, value, contextFields, startFieldId);
        FieldValue endValue = resolveValidatedOrContextValue(twinEntity, value, contextFields, endFieldId);

        if (durationValue == null || durationValue.isEmpty()
                || startValue == null || startValue.isEmpty()
                || endValue == null || endValue.isEmpty())
            return ValidationResult.VALID; // rule applies only when all three are filled

        if (!(durationValue instanceof FieldValueText durationText)
                || !(startValue instanceof FieldValueDate startDate)
                || !(endValue instanceof FieldValueDate endDate)) {
            log.error("Duration equals date diff misconfiguration for {}: duration/start/end have incompatible types",
                    value.getTwinClassField().logNormal());
            return new ValidationResult(false);
        }

        BigDecimal duration;
        try {
            duration = new BigDecimal(durationText.getValue().trim());
        } catch (NumberFormatException e) {
            log.error("Duration field {} value [{}] is not numeric", durationValue.getTwinClassField().logNormal(), durationText.getValue());
            return new ValidationResult(false);
        }

        long expectedDays = ChronoUnit.DAYS.between(startDate.getDate().toLocalDate(), endDate.getDate().toLocalDate());
        boolean isValid = duration.compareTo(BigDecimal.valueOf(expectedDays)) == 0;
        return isValid ? ValidationResult.VALID : new ValidationResult(false);
    }

    /**
     * Prefer the currently validated value when it belongs to the requested field,
     * otherwise resolve from payload/DB via {@link #resolveFieldValue}.
     */
    private FieldValue resolveValidatedOrContextValue(TwinEntity twinEntity, FieldValue validatedValue,
                                                      Map<UUID, FieldValue> contextFields, UUID twinClassFieldId) throws ServiceException {
        if (validatedValue != null && twinClassFieldId.equals(validatedValue.getTwinClassField().getId()))
            return validatedValue.isEmpty() ? null : validatedValue;
        return resolveFieldValue(twinEntity, contextFields, twinClassFieldId);
    }
}
