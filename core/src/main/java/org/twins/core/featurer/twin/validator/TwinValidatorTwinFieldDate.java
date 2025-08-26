package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1615,
        name = "Twin field date value",
        description = "")
public class TwinValidatorTwinFieldDate extends TwinValidator {
    private static final int DATE_FIELD_TYPER_ID = 1302;
    private static final String PARAMS_PATTERN = "pattern";

    @FeaturerParam(name = "Twin class field date id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldDateId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldDateId");

    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        twinService.loadTwinFields(twinEntity);

        UUID classFieldId = twinClassFieldDateId.extract(properties);

        TwinFieldSimpleEntity fieldEntity = twinEntity.getTwinFieldSimpleKit().get(classFieldId);
        if (fieldEntity == null) {
            throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, " can't find field of field class [" + classFieldId + "] for twin [" + twinEntity.logShort() + "]");
        }
        if (fieldEntity.getTwinClassField().getFieldTyperFeaturerId() != DATE_FIELD_TYPER_ID) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, " [" + classFieldId + "] is not a date field");
        }
        String fieldTimeString = fieldEntity.getValue();
        String pattern = fieldEntity.getTwinClassField().getFieldTyperParams().get(PARAMS_PATTERN);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime fieldTime = LocalDateTime.parse(fieldTimeString, formatter);

        LocalDateTime now = LocalDateTime.now();

        int comparison = fieldTime.compareTo(now);

        return buildResult(
                comparison < 0,
                invert,
                twinEntity.logShort() + " field of class [" + classFieldId + "] date value is in the future",
                twinEntity.logShort() + " field of class [" + classFieldId + "] date value is in the past");
    }
}
