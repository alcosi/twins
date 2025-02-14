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
import org.twins.core.domain.TwinField;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2321,
        name = "Field math difference from context field",
        description = "")
@Slf4j
@RequiredArgsConstructor
public class FillerFieldMathDifferenceFromContextField extends Filler {
    @FeaturerParam(name = "Minuend twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID minuendTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("minuendTwinClassFieldId");
    @FeaturerParam(name = "Subtrahend twin class field id", description = "Value from this field will be ", order = 2)
    public static final FeaturerParamUUID subtrahendTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("subtrahendTwinClassFieldId");
    @FeaturerParam(name = "Allow negative result", description = "", order = 3)
    public static final FeaturerParamBoolean allowNegativeResult = new FeaturerParamBoolean("allowNegativeResult");

    /**
     * A service for handling operations related to twin entities.
     * The TwinService is lazily initialized,
     * meaning its instantiation is deferred until it is necessary during runtime.
     * This behavior helps improve application startup time and resource utilization.
     */
    @Lazy
    private final TwinService twinService;

    /**
     * Populates specified properties and fields for a given factory item and template twin entity.
     * This method determines the subtraction result of two field values (minuend and subtrahend),
     * checks constraints like non-negative results if required, and updates the factory output
     * field with the resulting value.
     *
     * @param properties Configuration properties that provide additional parameters for the operation.
     *                   These properties include information such as field identifiers and allowable constraints.
     * @param factoryItem The factory item containing context and output data, which will be modified based on the operation logic.
     * @param templateTwin The template twin entity associated with the factory item. It serves as a reference for certain operations.
     * @throws ServiceException If an error occurs during field value extraction, type conversion, or constraint validation.
     */
    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID paramSubtrahendTwinClassFieldId = subtrahendTwinClassFieldId.extract(properties);
        UUID paramMinuendTwinClassFieldId = minuendTwinClassFieldId.extract(properties);
        FieldValue subtrahendFieldValue = fieldLookupers.getFromContextFieldsAndContextTwinDbFields().lookupFieldValue(factoryItem, paramSubtrahendTwinClassFieldId);
        if (!(subtrahendFieldValue instanceof FieldValueText)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "subtrahendTwinClassField[" + paramSubtrahendTwinClassFieldId + "] is not instance of text field and can not be converted to number");
        }

        FieldValue minuendFieldValue = factoryItem.getOutput().getField(paramMinuendTwinClassFieldId);
        if (minuendFieldValue == null && factoryItem.getOutput() instanceof TwinCreate)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory item is under creation and has no created field[" + paramMinuendTwinClassFieldId + "]");
        if (factoryItem.getOutput() instanceof TwinUpdate) { //perhaps we can get field value from database
            TwinField twinField = twinService.wrapField(factoryItem.getOutput().getTwinEntity(), paramMinuendTwinClassFieldId);
            if (twinField == null)
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "minuendTwinClassField[" + paramMinuendTwinClassFieldId + "] is not present in " + factoryItem.getOutput().getTwinEntity().getId());
            minuendFieldValue = twinService.getTwinFieldValue(twinField);
        }
        if (minuendFieldValue == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "minuendTwinClassField[" + paramMinuendTwinClassFieldId + "] can not be detected");

        if (minuendFieldValue instanceof FieldValueText minuedFieldValueText) {
            BigDecimal subtrahendNumber = new BigDecimal(((FieldValueText) subtrahendFieldValue).getValue());
            BigDecimal minuendNumber = new BigDecimal(minuedFieldValueText.getValue());
            log.trace("minuendNumber: {} and subtrahendNumber {}", minuendNumber, subtrahendNumber);
            BigDecimal difference = minuendNumber.subtract(subtrahendNumber);
            log.trace("difference = {}", difference);
            if (!allowNegativeResult.extract(properties) && difference.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Negative result detected, skipping difference");
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "negative difference result");
            }
            factoryItem.getOutput().addField(minuedFieldValueText.setValue(getProcessedString(difference)));
        } else {
            log.warn("Incorrect result detected, skipping difference");
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "minuendTwinClassField[" + paramMinuendTwinClassFieldId + "] is not instance of text field and can not be converted to number");
        }
    }

    /**
     * Converts a BigDecimal value into a string representation based on its scale.
     * If the value has no fractional part after rounding to the nearest integer,
     * it is represented without decimal places.
     * Otherwise, the original value's string representation is returned.
     *
     * @param difference the BigDecimal value to be converted to a string.
     * @return the string representation of the BigDecimal value, scaled appropriately.
     */
    private String getProcessedString(BigDecimal difference) {
        BigDecimal scaled = difference.setScale(0, RoundingMode.HALF_UP);
        if (difference.compareTo(scaled) == 0) {
            log.trace("Return scaled difference = {}", scaled);
           return scaled.toString();
        }
        log.trace("Return not scaled difference = {}", difference);
        return difference.toString();
    }
}
