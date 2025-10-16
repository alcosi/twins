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
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNumeric;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import static org.cambium.common.util.MathUtils.EXPONENTIAL_FORM_REGEXP;

@Component
@Slf4j
@Featurer(id = FeaturerTwins.ID_1317,
        name = "Numeric",
        description = "Numeric field")
public class FieldTyperNumeric extends FieldTyperSimple<FieldDescriptorNumeric, FieldValueText, TwinFieldSearchNumeric> {

    @FeaturerParam(name = "Min", description = "Min possible value", order = 1)
    public static final FeaturerParamDouble min = new FeaturerParamDouble("min");
    @FeaturerParam(name = "Max", description = "Max possible value", order = 2)
    public static final FeaturerParamDouble max = new FeaturerParamDouble("max");
    @FeaturerParam(name = "Step", description = "Step of value change", order = 3)
    public static final FeaturerParamDouble step = new FeaturerParamDouble("step");
    @FeaturerParam(name = "Thousand separator", description = "Thousand separator. Must not be equal to decimal separator.", order = 4, optional = true, defaultValue = " ")
    public static final FeaturerParamStringSet thousandSeparatorSet = new FeaturerParamStringSet("thousandSeparatorSet");
    @FeaturerParam(name = "Decimal separator", description = "Decimal separator. Must not be equal to thousand separator.", order = 5, optional = true, defaultValue = ".")
    public static final FeaturerParamStringSet decimalSeparatorSet = new FeaturerParamStringSet("decimalSeparatorSet");
    @FeaturerParam(name = "Decimal places", description = "Number of decimal places.", order = 6)
    public static final FeaturerParamInt decimalPlaces = new FeaturerParamInt("decimalPlaces");
    @FeaturerParam(name = "Round", description = "Round a number to the required number of decimal places", order = 7, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean round = new FeaturerParamBoolean("round");

    @Override
    public FieldDescriptorNumeric getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorNumeric()
                .min(min.extract(properties))
                .max(max.extract(properties))
                .step(step.extract(properties))
                .thousandSeparator(thousandSeparatorSet.extract(properties))
                .decimalSeparator(decimalSeparatorSet.extract(properties))
                .decimalPlaces(decimalPlaces.extract(properties))
                .round(round.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        Double minValue = min.extract(properties);
        Double maxValue = max.extract(properties);
        Double stepValue = step.extract(properties);
        Set<String> thousandSeparators = thousandSeparatorSet.extract(properties);
        Set<String> decimalSeparators = decimalSeparatorSet.extract(properties);
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

                int decimalSeparatorCount = 0;
                for (String decimalSep : decimalSeparators) {
                    decimalSeparatorCount += StringUtils.countMatches(finalValue, decimalSep);
                }
                if (decimalSeparatorCount > 1) {
                    log.error("FieldTyperNumeric: value[" + value.getValue() + "] has multiple decimal separators");
                    throw new Exception();
                }

                for (String thousandSeparator : thousandSeparators) {
                    finalValue = finalValue.replaceAll(Pattern.quote(thousandSeparator), "");
                }
                for (String decimalSeparator : decimalSeparators) {
                    finalValue = finalValue.replaceAll(Pattern.quote(decimalSeparator), ".");
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
                            " decSeparators:" + decimalSeparators + " thouSeparators:" + thousandSeparators + "].");
        }
        detectValueChange(twinFieldEntity, twinChangesCollector, finalValue);
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleEntity twinFieldEntity) {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(twinFieldEntity != null && twinFieldEntity.getValue() != null ? twinFieldEntity.getValue() : null);
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchNumeric search) throws ServiceException {
        return Specification.where(TwinSpecification.checkFieldNumeric(search));
    }
}
