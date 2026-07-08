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
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2361,
        name = "Field increment from context field",
        description = "")
@Slf4j
@RequiredArgsConstructor
public class FillerFieldIncrementFromContextField extends Filler {

    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "Allow negative increment", description = "If false, a negative context value fails the step (mark the step as optional to skip instead of failing the pipeline)", order = 2, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean allowNegativeIncrement = new FeaturerParamBoolean("allowNegativeIncrement");

    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID paramTwinClassFieldId = twinClassFieldId.extract(properties);

        FieldValue contextFieldValue = fieldLookupers.getFromContextFields().lookupFieldValue(factoryItem, paramTwinClassFieldId);
        if (!(contextFieldValue instanceof FieldValueText contextTextField) || contextTextField.isEmpty()) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "twinClassField[" + paramTwinClassFieldId + "] is not a filled numeric field in context and can not be used as increment delta");
        }

        BigDecimal delta;
        try {
            delta = new BigDecimal(contextTextField.getValue());
        } catch (NumberFormatException e) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "twinClassField[" + paramTwinClassFieldId + "] value[" + contextTextField.getValue() + "] is not a valid number");
        }
        if (!allowNegativeIncrement.extract(properties) && delta.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Negative increment delta detected, skipping increment");
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "negative increment delta[" + delta + "] is not allowed");
        }

        TwinClassFieldEntity field = twinClassFieldService.findEntitySafe(paramTwinClassFieldId);
        String incrementValue = (delta.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + delta.toPlainString();
        FieldValueText outputFieldValue = new FieldValueText(field).setValue(incrementValue);
        log.trace("Applying increment delta {} to twinClassField[{}]", incrementValue, paramTwinClassFieldId);
        factoryItem.getOutput().addField(outputFieldValue);
    }
}
