package org.twins.core.featurer.factory.filler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
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
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

/**
 * Sets a date field to the current date-time when the field is empty.
 * If the field already has a value, leaves it unchanged.
 */
@Component
@Featurer(id = FeaturerTwins.ID_2367,
        name = "Field date current",
        description = "Sets the target date field to now when it is empty; does not overwrite an existing value")
@Slf4j
@RequiredArgsConstructor
public class FillerFieldDateCurrent extends Filler {
    @FeaturerParam(name = "Twin class field id", description = "Date field to set to the current date-time when empty", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID fieldId = twinClassFieldId.extract(properties);
        FieldValue existing = fieldLookupers.getFromItemOutputFields().lookupFieldValue(factoryItem, fieldId);
        if (existing != null && existing.isNotEmpty()) {
            log.trace("twinClassField[{}] already filled, skip current date", fieldId);
            return;
        }
        FieldValue created = twinService.createFieldValue(twinClassFieldService.findEntitySafe(fieldId));
        if (!(created instanceof FieldValueDate dateValue)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR,
                    "twinClassField[" + fieldId + "] is not a date field");
        }
        dateValue.setDate(LocalDateTime.now());
        factoryItem.getOutput().addField(dateValue);
        log.trace("Set twinClassField[{}] to current date-time", fieldId);
    }
}
