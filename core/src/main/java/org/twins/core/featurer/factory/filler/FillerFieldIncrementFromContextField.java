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
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2361,
        name = "Field increment from context field",
        description = "")
@Slf4j
@RequiredArgsConstructor
public class FillerFieldIncrementFromContextField extends FillerFieldLookup {

    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "Allow negative increment", description = "If false, a negative context value fails the step (mark the step as optional to skip instead of failing the pipeline)", order = 2, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean allowNegativeIncrement = new FeaturerParamBoolean("allowNegativeIncrement");

    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    protected FieldLookuperNearest lookuper(Properties properties) {
        return fieldLookupers.getFromContextFields();
    }

    @Override
    protected UUID lookupFieldId(Properties properties) throws ServiceException {
        return twinClassFieldId.extract(properties);
    }

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin, FieldValue fieldValue) throws ServiceException {
        if (!(fieldValue instanceof FieldValueText fieldValueText)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "{} is incorrect type and can not be used as increment delta", fieldValue.getTwinClassField().logShort());
        } else if (fieldValue.isEmpty()) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "{} is is empty and can not be used as increment delta", fieldValue.getTwinClassField().logShort());
        }

        BigDecimal delta;
        try {
            delta = new BigDecimal(fieldValueText.getValue());
        } catch (NumberFormatException e) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, fieldValue.getTwinClassField().logShort() + " value[" + fieldValueText.getValue() + "] is not a valid number");
        }
        if (!allowNegativeIncrement.extract(properties) && delta.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Negative increment delta detected, skipping increment");
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "negative increment delta[" + delta + "] is not allowed");
        }

        String incrementValue = (delta.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + delta.toPlainString();
        FieldValueText outputFieldValue = new FieldValueText(fieldValue.getTwinClassField()).setValue(incrementValue);
        log.trace("Applying increment delta {} to {}", incrementValue, fieldValue.getTwinClassField().logShort());
        factoryItem.getOutput().addField(outputFieldValue);
    }
}
