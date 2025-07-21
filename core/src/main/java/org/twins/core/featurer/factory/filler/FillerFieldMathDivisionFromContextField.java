package org.twins.core.featurer.factory.filler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.BigDecimalUtil;
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
@Featurer(id = FeaturerTwins.ID_2340,
        name = "Field math division from context field",
        description = "")
@Slf4j
@RequiredArgsConstructor
public class FillerFieldMathDivisionFromContextField extends Filler {
    @FeaturerParam(name = "Dividend twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID dividendTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dividendTwinClassFieldId");
    @FeaturerParam(name = "Divisor twin class field id", description = "", order = 2)
    public static final FeaturerParamUUID divisorTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("divisorTwinClassFieldId");
    @FeaturerParam(name = "Target twin class field id", description = "", order = 3)
    public static final FeaturerParamUUID targetTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("targetTwinClassFieldId");
    @FeaturerParam(name = "Allow negative result", description = "", order = 4)
    public static final FeaturerParamBoolean allowNegativeResult = new FeaturerParamBoolean("allowNegativeResult");

    @Lazy
    private final TwinService twinService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID paramDividendTwinClassFieldId = dividendTwinClassFieldId.extract(properties);
        UUID paramDivisorTwinClassFieldId = divisorTwinClassFieldId.extract(properties);
        UUID paramTargetTwinClassFieldId = targetTwinClassFieldId.extract(properties);
        FieldValue dividendFieldValue = factoryItem.getOutput().getField(paramDividendTwinClassFieldId);
        if (dividendFieldValue == null) {
            dividendFieldValue = fieldLookupers.getFromContextFieldsAndContextTwinDbFields().lookupFieldValue(factoryItem, paramDividendTwinClassFieldId);
        }
        if (!(dividendFieldValue instanceof FieldValueText)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "dividendTwinClassField[" + paramDividendTwinClassFieldId + "] is not instance of text field and can not be converted to number");
        }

        FieldValue divisorFieldValue = fieldLookupers.getFromContextFieldsAndContextTwinDbFields().lookupFieldValue(factoryItem, paramDivisorTwinClassFieldId);
        if (divisorFieldValue == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "divisorTwinClassField[" + paramDivisorTwinClassFieldId + "] can not be detected");

        if (divisorFieldValue instanceof FieldValueText divisorFieldValueText) {
            BigDecimal dividendNumber = new BigDecimal(((FieldValueText) dividendFieldValue).getValue());
            BigDecimal divisorNumber = new BigDecimal(divisorFieldValueText.getValue() == null ? "0" : divisorFieldValueText.getValue());
            if (divisorNumber.compareTo(BigDecimal.ZERO) == 0) {
                log.warn("Division by zero detected, skipping division");
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "division by zero");
            }
            log.trace("divisorNumber: {} and dividendNumber {}", divisorNumber, dividendNumber);
            BigDecimal division = dividendNumber.divide(divisorNumber, 2, RoundingMode.HALF_UP);
            log.trace("division = {}", division);
            if (!allowNegativeResult.extract(properties) && division.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Negative result detected, skipping division");
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "negative division result");
            }

            FieldValue targetFieldValue = factoryItem.getOutput().getField(paramTargetTwinClassFieldId);
            if (targetFieldValue == null && factoryItem.getOutput() instanceof TwinCreate)
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory item is under creation and has no created field[" + paramTargetTwinClassFieldId + "]");
            if (factoryItem.getOutput() instanceof TwinUpdate) {
                TwinField twinField = twinService.wrapField(factoryItem.getOutput().getTwinEntity(), paramTargetTwinClassFieldId);
                if (twinField == null)
                    throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "targetTwinClassField[" + paramTargetTwinClassFieldId + "] is not present in " + factoryItem.getOutput().getTwinEntity().getId());
                targetFieldValue = twinService.getTwinFieldValue(twinField);
            }
            if (targetFieldValue == null)
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "targetTwinClassField[" + paramTargetTwinClassFieldId + "] can not be detected");

            if (targetFieldValue instanceof FieldValueText targetFieldValueText) {
                factoryItem.getOutput().addField(targetFieldValueText.setValue(BigDecimalUtil.getProcessedString(division)));
            } else {
                log.warn("Incorrect result detected, skipping division");
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "targetTwinClassField[" + paramTargetTwinClassFieldId + "] is not instance of text field and can not be converted to number");
            }

        } else {
            log.warn("Incorrect result detected, skipping division");
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "divisorTwinClassField[" + paramDivisorTwinClassFieldId + "] is not instance of text field and can not be converted to number");
        }
    }
} 