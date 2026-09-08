package org.twins.core.featurer.factory.filler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

/**
 * Sets a date field from another date ± inclusive duration days.
 * Inclusive: {@code end = start + (duration - 1)} days, {@code start = end - (duration - 1)} days
 * (same semantics as FieldValidatorDurationEqualsDateDiff / 5602).
 * Skips when the target is already filled or required source values are missing.
 */
@Component
@Featurer(id = FeaturerTwins.ID_2368,
        name = "Field date shift by duration",
        description = "Sets target date from source date ± (duration - 1) inclusive days when target is empty")
@Slf4j
@RequiredArgsConstructor
public class FillerFieldDateShiftByDuration extends Filler {
    @FeaturerParam(name = "Target twin class field id", description = "Date field to fill when empty", order = 1)
    public static final FeaturerParamUUID targetTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("targetTwinClassFieldId");

    @FeaturerParam(name = "Source date twin class field id", description = "Date field used as the shift base", order = 2)
    public static final FeaturerParamUUID sourceDateTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("sourceDateTwinClassFieldId");

    @FeaturerParam(name = "Duration twin class field id", description = "Numeric duration in inclusive days", order = 3)
    public static final FeaturerParamUUID durationTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("durationTwinClassFieldId");

    @FeaturerParam(name = "Subtract duration", description = "If true, target = source - (duration - 1); if false, target = source + (duration - 1)", order = 4, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean subtractDuration = new FeaturerParamBoolean("subtractDuration");

    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID targetFieldId = targetTwinClassFieldId.extract(properties);
        UUID sourceFieldId = sourceDateTwinClassFieldId.extract(properties);
        UUID durationFieldId = durationTwinClassFieldId.extract(properties);

        FieldValue targetExisting = fieldLookupers.getFromItemOutputFields().lookupFieldValue(factoryItem, targetFieldId);
        if (targetExisting != null && targetExisting.isNotEmpty()) {
            log.trace("target twinClassField[{}] already filled, skip date shift", targetFieldId);
            return;
        }

        FieldValue sourceValue = fieldLookupers.getFromItemOutputFields().lookupFieldValue(factoryItem, sourceFieldId);
        FieldValue durationValue = fieldLookupers.getFromItemOutputFields().lookupFieldValue(factoryItem, durationFieldId);
        if (!(sourceValue instanceof FieldValueDate sourceDate) || sourceDate.isEmpty()
                || !(durationValue instanceof FieldValueText durationText) || durationText.isEmpty()) {
            log.trace("source date[{}] or duration[{}] missing, skip date shift into [{}]", sourceFieldId, durationFieldId, targetFieldId);
            return;
        }

        BigDecimal duration;
        try {
            duration = new BigDecimal(durationText.getValue().trim());
        } catch (NumberFormatException e) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR,
                    "duration twinClassField[" + durationFieldId + "] value[" + durationText.getValue() + "] is not numeric");
        }
        if (duration.compareTo(BigDecimal.ONE) < 0) {
            log.warn("duration twinClassField[{}] value[{}] < 1, skip date shift", durationFieldId, duration);
            return;
        }

        long shiftDays = duration.longValue() - 1;
        LocalDateTime result = Boolean.TRUE.equals(subtractDuration.extract(properties))
                ? sourceDate.getDate().minusDays(shiftDays)
                : sourceDate.getDate().plusDays(shiftDays);

        FieldValue created = twinService.createFieldValue(twinClassFieldService.findEntitySafe(targetFieldId));
        if (!(created instanceof FieldValueDate targetDate)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR,
                    "target twinClassField[" + targetFieldId + "] is not a date field");
        }
        targetDate.setDate(result);
        factoryItem.getOutput().addField(targetDate);
        log.trace("Set twinClassField[{}] = {} from source[{}] duration[{}]", targetFieldId, result, sourceFieldId, durationFieldId);
    }
}
