package org.twins.core.featurer.factory.filler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.BigDecimalUtil;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
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
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.UUID;

/**
 * Sets a duration field to inclusive day count between two date fields:
 * {@code (end date - start date + 1)}. Skips when the duration is already filled
 * or either date is missing.
 */
@Component
@Featurer(id = FeaturerTwins.ID_2369,
        name = "Field duration between dates",
        description = "Sets duration = (end - start + 1) inclusive days when duration is empty and both dates are filled")
@Slf4j
@RequiredArgsConstructor
public class FillerFieldDurationBetweenDates extends Filler {
    @FeaturerParam(name = "Duration twin class field id", description = "Numeric duration field to fill when empty", order = 1)
    public static final FeaturerParamUUID durationTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("durationTwinClassFieldId");

    @FeaturerParam(name = "Start date twin class field id", description = "Start date field", order = 2)
    public static final FeaturerParamUUID startDateTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("startDateTwinClassFieldId");

    @FeaturerParam(name = "End date twin class field id", description = "End date field", order = 3)
    public static final FeaturerParamUUID endDateTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("endDateTwinClassFieldId");

    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID durationFieldId = durationTwinClassFieldId.extract(properties);
        UUID startFieldId = startDateTwinClassFieldId.extract(properties);
        UUID endFieldId = endDateTwinClassFieldId.extract(properties);

        FieldValue durationExisting = fieldLookupers.getFromItemOutputFields().lookupFieldValue(factoryItem, durationFieldId);
        if (durationExisting != null && durationExisting.isNotEmpty()) {
            log.trace("duration twinClassField[{}] already filled, skip", durationFieldId);
            return;
        }

        FieldValue startValue = fieldLookupers.getFromItemOutputFields().lookupFieldValue(factoryItem, startFieldId);
        FieldValue endValue = fieldLookupers.getFromItemOutputFields().lookupFieldValue(factoryItem, endFieldId);
        if (!(startValue instanceof FieldValueDate startDate) || startDate.isEmpty()
                || !(endValue instanceof FieldValueDate endDate) || endDate.isEmpty()) {
            log.trace("start[{}] or end[{}] missing, skip duration into [{}]", startFieldId, endFieldId, durationFieldId);
            return;
        }

        long days = ChronoUnit.DAYS.between(startDate.getDate().toLocalDate(), endDate.getDate().toLocalDate()) + 1;
        if (days < 1) {
            log.warn("computed duration days[{}] < 1 for start[{}] end[{}], skip", days, startFieldId, endFieldId);
            return;
        }

        FieldValue created = twinService.createFieldValue(twinClassFieldService.findEntitySafe(durationFieldId));
        if (!(created instanceof FieldValueText durationText)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR,
                    "duration twinClassField[" + durationFieldId + "] is not a numeric/text field");
        }
        durationText.setValue(BigDecimalUtil.getProcessedString(BigDecimal.valueOf(days)));
        factoryItem.getOutput().addField(durationText);
        log.trace("Set duration twinClassField[{}] = {}", durationFieldId, days);
    }
}
