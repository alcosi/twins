package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNumeric;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

import static org.cambium.common.util.MathUtils.EXPONENTIAL_FORM_REGEXP;

@Component
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_1317,
        name = "Decimal",
        description = "Decimal field with dedicated table storage"
)
public class FieldTyperDecimal extends FieldTyperDecimalBase<FieldDescriptorNumeric, FieldValueText, TwinFieldSearchNumeric> {
    
    @FeaturerParam(name = "Min", description = "Min possible value", order = 1)
    public static final FeaturerParamDouble min = new FeaturerParamDouble("min");
    @FeaturerParam(name = "Max", description = "Max possible value", order = 2)
    public static final FeaturerParamDouble max = new FeaturerParamDouble("max");
    @FeaturerParam(name = "Step", description = "Step of value change", order = 3)
    public static final FeaturerParamDouble step = new FeaturerParamDouble("step");
    @FeaturerParam(name = "Thousand separator", description = "Thousand separator. Must not be equal to decimal separator.", order = 4)
    public static final FeaturerParamString thousandSeparator = new FeaturerParamString("thousandSeparator");
    @FeaturerParam(name = "Decimal separator", description = "Decimal separator. Must not be equal to thousand separator.", order = 5)
    public static final FeaturerParamString decimalSeparator = new FeaturerParamString("decimalSeparator");
    @FeaturerParam(name = "Decimal places", description = "Number of decimal places.", order = 6)
    public static final FeaturerParamInt decimalPlaces = new FeaturerParamInt("decimalPlaces");
    @FeaturerParam(name = "Extra thousand separators", description = "Extra thousand separators. Must not be equal to decimal separator.", order = 7, optional = true)
    public static final FeaturerParamStringSet extraThousandSeparatorSet = new FeaturerParamStringSet("extraThousandSeparatorSet");
    @FeaturerParam(name = "Extra decimal separators", description = "Extra decimal separators. Must not be equal to thousand separator.", order = 8, optional = true)
    public static final FeaturerParamStringSet extraDecimalSeparatorSet = new FeaturerParamStringSet("extraDecimalSeparatorSet");
    @FeaturerParam(name = "Round", description = "Round a number to the required number of decimal places", order = 9, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean round = new FeaturerParamBoolean("round");

    @Override
    public FieldDescriptorNumeric getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorNumeric()
                .min(min.extract(properties))
                .max(max.extract(properties))
                .step(step.extract(properties))
                .thousandSeparator(thousandSeparator.extract(properties))
                .decimalSeparator(decimalSeparator.extract(properties))
                .decimalPlaces(decimalPlaces.extract(properties))
                .round(round.extract(properties))
                .extraThousandSeparators(extraThousandSeparatorSet.extract(properties))
                .extraDecimalSeparators(extraDecimalSeparatorSet.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldDecimalEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        Double minValue = min.extract(properties);
        Double maxValue = max.extract(properties);
        Double stepValue = step.extract(properties);
        String thousandSeparatorValue = thousandSeparator.extract(properties);
        String decimalSeparatorValue = decimalSeparator.extract(properties);
        Set<String> extraThousandSeparators = Optional.ofNullable(extraThousandSeparatorSet.extract(properties)).orElse(Collections.emptySet());
        Set<String> extraDecimalSeparators = Optional.ofNullable(extraDecimalSeparatorSet.extract(properties)).orElse(Collections.emptySet());
        Integer decimalPlacesValue = decimalPlaces.extract(properties);
        Boolean roundValue = round.extract(properties);

        String finalValue;
        try {
            finalValue = value.getValue();
            if (!StringUtils.isBlank(finalValue)) {
                if (finalValue.matches(EXPONENTIAL_FORM_REGEXP)) {
                    DecimalFormat df = new DecimalFormat("#.############");
                    finalValue = df.format(Double.parseDouble(finalValue));
                }

                // Combine main decimal separator with extra ones for counting
                Set<String> allDecimalSeparators = new HashSet<>();
                allDecimalSeparators.add(decimalSeparatorValue);
                allDecimalSeparators.addAll(extraDecimalSeparators);

                int decimalSeparatorCount = 0;
                for (String decimalSep : allDecimalSeparators) {
                    decimalSeparatorCount += StringUtils.countMatches(finalValue, decimalSep);
                }
                if (decimalSeparatorCount > 1) {
                    log.error("FieldTyperNumeric: value[" + value.getValue() + "] has multiple decimal separators");
                    throw new Exception();
                }

                // Combine main thousand separator with extra ones for removal
                Set<String> allThousandSeparators = new HashSet<>();
                allThousandSeparators.add(thousandSeparatorValue);
                allThousandSeparators.addAll(extraThousandSeparators);

                // Remove all thousand separators
                for (String thousandSep : allThousandSeparators) {
                    finalValue = finalValue.replaceAll(Pattern.quote(thousandSep), "");
                }

                // Replace all decimal separators with dot
                for (String decimalSep : allDecimalSeparators) {
                    finalValue = finalValue.replaceAll(Pattern.quote(decimalSep), ".");
                }

                String[] parts = finalValue.split("\\.");
                String integerPart = parts[0];
                String decimalPart = parts.length > 1 ? parts[1] : "";

                if (decimalPart.length() > decimalPlacesValue) {
                    if (Boolean.FALSE.equals(roundValue)) {
                        log.error("FieldTyperNumeric: value[" + value.getValue() + "] has more decimal places then parametrized");
                        throw new Exception();
                    }
                    decimalPart = decimalPart.substring(0, decimalPlacesValue);
                } else if (decimalPart.length() < decimalPlacesValue) {
                    decimalPart = StringUtils.rightPad(decimalPart, decimalPlacesValue, '0');
                }

                if (decimalPlacesValue > 0) {
                    finalValue = integerPart + "." + decimalPart;
                } else {
                    finalValue = integerPart;
                }

                double doubleValue = Double.parseDouble(finalValue);
                if ((minValue != null && doubleValue < minValue) || (maxValue != null && doubleValue > maxValue)) {
                    log.error("FieldTyperNumeric: value[" + value.getValue() + "] is out of range");
                    throw new Exception();
                }
            } // else value setting null
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) +
                            " value[" + value.getValue() + "] is not numeric format or does not match the field settings[" +
                            " min:" + minValue + " max:" + maxValue + " step:" + stepValue + " decPlaces:" + decimalPlacesValue +
                            " thousandSep:" + thousandSeparatorValue + " extraThousandSep:" + extraThousandSeparators +
                            " decimalSep:" + decimalSeparatorValue + " extraDecimalSep:" + extraDecimalSeparators + "].");
        }
        detectValueChange(twinFieldEntity, twinChangesCollector, new BigDecimal(finalValue));
    }


    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldEntity) {
        var scale = decimalPlaces.extract(properties);
        var value = "";

        if (scale != null) {
            value = twinFieldEntity != null && twinFieldEntity.getValue() != null ? twinFieldEntity.getValue().setScale(scale, RoundingMode.UNNECESSARY).toPlainString() : null;
        } else {
            value = twinFieldEntity != null && twinFieldEntity.getValue() != null ? twinFieldEntity.getValue().toString() : null;
        }

        return new FieldValueText(twinField.getTwinClassField()).setValue(value);
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchNumeric search) throws ServiceException {
        return TwinSpecification.checkFieldDecimal(search);
    }
}
