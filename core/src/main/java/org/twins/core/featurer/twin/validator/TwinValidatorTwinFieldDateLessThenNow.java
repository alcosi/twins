package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1615,
        name = "Twin field date value",
        description = "")
public class TwinValidatorTwinFieldDateLessThenNow extends TwinValidator {
    @FeaturerParam(name = "Twin class field date id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldDateId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldDateId");

    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        twinService.loadFieldsValues(twinEntity);

        UUID classFieldId = twinClassFieldDateId.extract(properties);
        boolean isValid;

        FieldValue fieldValue = twinEntity.getFieldValuesKit().get(classFieldId);
        if (fieldValue == null || fieldValue.isEmpty()) {
            log.error("twinClassField[{}] was not found for {}", classFieldId, twinEntity.logShort());
            isValid = false;
            //todo exception??
        } else if (fieldValue instanceof FieldValueDate fieldValueDate) {
            isValid = fieldValueDate.getDate().isBefore(LocalDateTime.now());
        } else {
            log.warn("{} is not a date field", fieldValue.getTwinClassField().logNormal());
            isValid = false;
            //todo exception??
        }


        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " field of class [" + classFieldId + "] date value is in the future",
                twinEntity.logShort() + " field of class [" + classFieldId + "] date value is in the past");
    }
}
