package org.twins.core.featurer.recomputer;

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
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.recompute.FieldRecomputeRequest;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.UUID;

/**
 * Recomputes the subscriber duration field as {@code (end - start + 1)} inclusive days
 * (same semantics as filler 2369 / validator 5602). Skips when either date is missing.
 * Always overwrites the subscriber field (field-edit recalculation, not fill-if-empty).
 */
@Component
@Featurer(id = FeaturerTwins.ID_5504,
        name = "Recomputer field duration between dates",
        description = "Sets subscriber duration = (end - start + 1) inclusive days when both dates are present")
@Slf4j
@RequiredArgsConstructor
public class RecomputerFieldDurationBetweenDates extends Recomputer {

    @FeaturerParam(name = "Start date twin class field id", description = "Start date field", order = 1)
    public static final FeaturerParamUUID startDateTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("startDateTwinClassFieldId");

    @FeaturerParam(name = "End date twin class field id", description = "End date field", order = 2)
    public static final FeaturerParamUUID endDateTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("endDateTwinClassFieldId");

    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public void recompute(FieldRecomputeRequest request, TwinChangesCollector collector, Properties properties) throws ServiceException {
        TwinEntity twin = request.subscriberTwin();
        TwinClassFieldEntity durationField = request.subscriberField();
        UUID startFieldId = startDateTwinClassFieldId.extract(properties);
        UUID endFieldId = endDateTwinClassFieldId.extract(properties);

        twinService.loadTwinFields(twin);

        Timestamp startTs = twinClassFieldService.getTimestampValue(twin, startFieldId, null);
        Timestamp endTs = twinClassFieldService.getTimestampValue(twin, endFieldId, null);
        if (startTs == null || endTs == null) {
            log.trace("start[{}] or end[{}] missing on twin[{}], skip duration into [{}]",
                    startFieldId, endFieldId, twin.getId(), durationField.getId());
            return;
        }

        long days = ChronoUnit.DAYS.between(startTs.toLocalDateTime().toLocalDate(), endTs.toLocalDateTime().toLocalDate()) + 1;
        if (days < 1) {
            log.warn("computed duration days[{}] < 1 for start[{}] end[{}], skip", days, startFieldId, endFieldId);
            return;
        }

        FieldValueText value = new FieldValueText(durationField)
                .setValue(BigDecimalUtil.getProcessedString(BigDecimal.valueOf(days)));
        FieldTyper fieldTyper = featurerService.getFeaturer(durationField.getFieldTyperFeaturerId(), FieldTyper.class);
        fieldTyper.serializeValue(twin, value, collector);
        log.trace("Recomputed duration twinClassField[{}] = {}", durationField.getId(), days);
    }
}
