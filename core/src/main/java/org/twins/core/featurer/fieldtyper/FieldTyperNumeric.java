package org.twins.core.featurer.fieldtyper;

import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamDouble;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;
import java.util.regex.Pattern;

@Component
@Featurer(id = FeaturerTwins.ID_1317,
        name = "FieldTyperNumeric",
        description = "Numeric field")
public class FieldTyperNumeric extends FieldTyperSimple<FieldDescriptorNumeric, FieldValueText> {

    @FeaturerParam(name = "min", description = "Min possible value")
    public static final FeaturerParamDouble min = new FeaturerParamDouble("min");
    @FeaturerParam(name = "max", description = "Max possible value")
    public static final FeaturerParamDouble max = new FeaturerParamDouble("max");
    @FeaturerParam(name = "step", description = "Step of value change")
    public static final FeaturerParamDouble step = new FeaturerParamDouble("step");
    @FeaturerParam(name = "thousandSeparator", description = "Thousand separator. Must not be equal to decimal separator.")
    public static final FeaturerParamString thousandSeparator = new FeaturerParamString("thousandSeparator");
    @FeaturerParam(name = "decimalSeparator", description = "Decimal separator. Must not be equal to thousand separator.")
    public static final FeaturerParamString decimalSeparator = new FeaturerParamString("decimalSeparator");
    @FeaturerParam(name = "decimalPlaces", description = "Number of decimal places.")
    public static final FeaturerParamInt decimalPlaces = new FeaturerParamInt("decimalPlaces");

    @Override
    public FieldDescriptorNumeric getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorNumeric()
                .min(min.extract(properties))
                .max(max.extract(properties))
                .step(step.extract(properties))
                .thousandSeparator(thousandSeparator.extract(properties))
                .decimalSeparator(decimalSeparator.extract(properties))
                .decimalPlaces(decimalPlaces.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinFieldEntity.getTwinClassField().isRequired() && StringUtils.isEmpty(value.getValue()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        Double minValue = min.extract(properties);
        Double maxValue = max.extract(properties);
        Double stepValue = step.extract(properties);
        String thousandSeparatorValue = thousandSeparator.extract(properties);
        String decimalSeparatorValue = decimalSeparator.extract(properties);
        Integer decimalPlacesValue = decimalPlaces.extract(properties);
        String finalValue;
        try {
            finalValue = value.getValue();
            if (finalValue.matches("[+-]?[0-9]+(\\.[0-9]+)?[eE][+-]?[0-9]+"))
                finalValue = Double.toString(Double.parseDouble(finalValue));
            finalValue.replaceAll(Pattern.quote(thousandSeparatorValue), "")
                    .replaceAll(Pattern.quote(decimalSeparatorValue), ".");
            String[] parts = finalValue.split("\\.");
            if ((null != decimalPlacesValue && parts.length > 1 && parts[1].length() > decimalPlacesValue))
                throw new Exception();
            double doubleValue = Double.parseDouble(finalValue);
            if ((null != minValue && doubleValue < minValue) || (null != maxValue && doubleValue > maxValue))
                throw new Exception();
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) +
                            " value[" + value.getValue() + "] is not numeric format or does not match the field settings[" +
                            " min:" + minValue + " max:" + maxValue + " step:" + stepValue + " decPlaces:" + decimalPlacesValue +
                            " decSeparator:" + decimalSeparatorValue + " thouSeparator:" + thousandSeparatorValue + "].");
        }
        detectValueChange(twinFieldEntity, twinChangesCollector, finalValue);
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleEntity twinFieldEntity) {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(twinFieldEntity != null && twinFieldEntity.getValue() != null ? twinFieldEntity.getValue() : null);
    }
}
