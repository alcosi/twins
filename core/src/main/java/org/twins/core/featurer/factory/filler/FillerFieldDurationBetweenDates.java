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
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.factory.lookuper.LookupResult;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamStringTwinsFactoryFieldLookuper;
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

    @FeaturerParam(name = "Field lookuper", description = "Source of the field values", order = 99, optional = true, defaultValue = "fromItemOutputFields")
    public static final FeaturerParamStringTwinsFactoryFieldLookuper fieldLookuperParam = new FeaturerParamStringTwinsFactoryFieldLookuper("fieldLookuper");

    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    /**
     * Direct batch override (not a {@code FillerAtomic} subclass): one lookuper batch call per field
     * (bulk preloads + entity resolution once), then the per-item distribution — each result's
     * failure is re-thrown at the exact point where the old per-item body performed the lookup (the
     * start/end failures only surface when the duration was not already filled) — see
     * featurer_design_pattern.md.
     */
    @Override
    public void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        UUID durationFieldId = durationTwinClassFieldId.extract(properties);
        UUID startFieldId = startDateTwinClassFieldId.extract(properties);
        UUID endFieldId = endDateTwinClassFieldId.extract(properties);
        FieldLookuperNearest lookuper = (FieldLookuperNearest) fieldLookupers.getByType(fieldLookuperParam.extract(properties));
        LookupResult durationResult = lookuper.lookupFieldValue(batch, durationFieldId);
        LookupResult startResult = lookuper.lookupFieldValue(batch, startFieldId);
        LookupResult endResult = lookuper.lookupFieldValue(batch, endFieldId);
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                fillWith(factoryItem, properties, durationFieldId, startFieldId, endFieldId, durationResult, startResult, endResult);
            } catch (Exception ex) {
                handleItemError(factoryItem, optionalStep, ex);
            }
        }
    }

    /** Per-item body with the pre-resolved lookup results — see the batch override above. */
    private void fillWith(FactoryItem factoryItem, Properties properties, UUID durationFieldId, UUID startFieldId, UUID endFieldId,
                          LookupResult durationResult, LookupResult startResult, LookupResult endResult) throws ServiceException {
        durationResult.rethrowFailureIfPresent(factoryItem);
        FieldValue durationExisting = durationResult.value(factoryItem);
        if (durationExisting != null && durationExisting.isNotEmpty()) {
            log.info("duration twinClassField[{}] already filled, skip", durationFieldId);
            return;
        }

        startResult.rethrowFailureIfPresent(factoryItem);
        endResult.rethrowFailureIfPresent(factoryItem);
        FieldValue startValue = startResult.value(factoryItem);
        FieldValue endValue = endResult.value(factoryItem);
        if (!(startValue instanceof FieldValueDate startDate) || startDate.isEmpty()
                || !(endValue instanceof FieldValueDate endDate) || endDate.isEmpty()) {
            log.info("start[{}] or end[{}] missing, skip duration into [{}]", startFieldId, endFieldId, durationFieldId);
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
        log.info("Set duration twinClassField[{}] = {}", durationFieldId, days);
    }
}
