package org.twins.core.featurer.recomputer;

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
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.recompute.FieldRecomputeRequest;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

/**
 * Recomputes the subscriber date field as {@code sourceDate ± (duration - 1)} inclusive days
 * (same semantics as filler 2368 / validator 5602). Skips when any operand is missing.
 * Always overwrites the subscriber field (field-edit recalculation, not fill-if-empty).
 */
@Component
@Featurer(id = FeaturerTwins.ID_5503,
        name = "Recomputer field date shift by duration",
        description = "Sets subscriber date = source date ± (duration - 1) inclusive days when both operands are present")
@Slf4j
@RequiredArgsConstructor
public class RecomputerFieldDateShiftByDuration extends Recomputer {

    @FeaturerParam(name = "Source date twin class field id", description = "Date field used as the shift base", order = 1)
    public static final FeaturerParamUUID sourceDateTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("sourceDateTwinClassFieldId");

    @FeaturerParam(name = "Duration twin class field id", description = "Numeric duration in inclusive days", order = 2)
    public static final FeaturerParamUUID durationTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("durationTwinClassFieldId");

    @FeaturerParam(name = "Subtract duration", description = "If true, target = source - (duration - 1); if false, target = source + (duration - 1)", order = 3, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean subtractDuration = new FeaturerParamBoolean("subtractDuration");

    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public void recompute(FieldRecomputeRequest request, TwinChangesCollector collector, Properties properties) throws ServiceException {
        TwinEntity twin = request.subscriberTwin();
        TwinClassFieldEntity targetField = request.subscriberField();
        UUID sourceFieldId = sourceDateTwinClassFieldId.extract(properties);
        UUID durationFieldId = durationTwinClassFieldId.extract(properties);

        twinService.loadTwinFields(twin);

        Timestamp sourceTs = twinClassFieldService.getTimestampValue(twin, sourceFieldId, null);
        BigDecimal duration = twinClassFieldService.getDecimalValue(twin, durationFieldId, null);
        if (sourceTs == null || duration == null) {
            log.trace("source date[{}] or duration[{}] missing on twin[{}], skip date shift into [{}]",
                    sourceFieldId, durationFieldId, twin.getId(), targetField.getId());
            return;
        }
        if (duration.compareTo(BigDecimal.ONE) < 0) {
            log.warn("duration twinClassField[{}] value[{}] < 1, skip date shift", durationFieldId, duration);
            return;
        }

        long shiftDays = duration.longValue() - 1;
        LocalDateTime result = Boolean.TRUE.equals(subtractDuration.extract(properties))
                ? sourceTs.toLocalDateTime().minusDays(shiftDays)
                : sourceTs.toLocalDateTime().plusDays(shiftDays);

        String pattern = twinClassFieldService.getDateFieldPattern(targetField);
        FieldValueDate value = new FieldValueDate(targetField, pattern).setDate(result);
        FieldTyper fieldTyper = featurerService.getFeaturer(targetField.getFieldTyperFeaturerId(), FieldTyper.class);
        fieldTyper.serializeValue(twin, value, collector);
        log.trace("Recomputed twinClassField[{}] = {} from source[{}] duration[{}]", targetField.getId(), result, sourceFieldId, durationFieldId);
    }
}
