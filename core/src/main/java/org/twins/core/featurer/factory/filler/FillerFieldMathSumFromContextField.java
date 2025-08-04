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
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2339,
        name = "Field math sum from context field",
        description = "")
@Slf4j
@RequiredArgsConstructor
public class FillerFieldMathSumFromContextField extends Filler {
    @FeaturerParam(name = "Addend twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID addendTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("addendTwinClassFieldId");
    @FeaturerParam(name = "Augend twin class field id", description = "", order = 2)
    public static final FeaturerParamUUID augendTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("augendTwinClassFieldId");
    @FeaturerParam(name = "Allow negative result", description = "", order = 3)
    public static final FeaturerParamBoolean allowNegativeResult = new FeaturerParamBoolean("allowNegativeResult");

    @Lazy
    private final TwinService twinService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID paramAddendTwinClassFieldId = addendTwinClassFieldId.extract(properties);
        UUID paramAugendTwinClassFieldId = augendTwinClassFieldId.extract(properties);
        FieldValue addendFieldValue = fieldLookupers.getFromContextFieldsAndContextTwinDbFields().lookupFieldValue(factoryItem, paramAddendTwinClassFieldId);
        if (!(addendFieldValue instanceof FieldValueText)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "addendTwinClassField[" + paramAddendTwinClassFieldId + "] is not instance of text field and can not be converted to number");
        }

        FieldValue augendFieldValue = factoryItem.getOutput().getField(paramAugendTwinClassFieldId);
        if (augendFieldValue == null && factoryItem.getOutput() instanceof TwinCreate)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Factory item is under creation and has no created field[" + paramAugendTwinClassFieldId + "]");
        if (factoryItem.getOutput() instanceof TwinUpdate) {
            TwinField twinField = twinService.wrapField(factoryItem.getOutput().getTwinEntity(), paramAugendTwinClassFieldId);
            if (twinField == null)
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "augendTwinClassField[" + paramAugendTwinClassFieldId + "] is not present in " + factoryItem.getOutput().getTwinEntity().getId());
            augendFieldValue = twinService.getTwinFieldValue(twinField);
        }
        if (augendFieldValue == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "augendTwinClassField[" + paramAugendTwinClassFieldId + "] can not be detected");

        if (augendFieldValue instanceof FieldValueText augendFieldValueText) {
            BigDecimal addendNumber = new BigDecimal(((FieldValueText) addendFieldValue).getValue());
            BigDecimal augendNumber = new BigDecimal(augendFieldValueText.getValue() == null ? "0" : augendFieldValueText.getValue());
            log.trace("augendNumber: {} and addendNumber {}", augendNumber, addendNumber);
            BigDecimal sum = augendNumber.add(addendNumber);
            log.trace("sum = {}", sum);
            if (!allowNegativeResult.extract(properties) && sum.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Negative result detected, skipping sum");
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "negative sum result");
            }
            factoryItem.getOutput().addField(augendFieldValueText.setValue(BigDecimalUtil.getProcessedString(sum)));
        } else {
            log.warn("Incorrect result detected, skipping sum");
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "augendTwinClassField[" + paramAugendTwinClassFieldId + "] is not instance of text field and can not be converted to number");
        }
    }
} 