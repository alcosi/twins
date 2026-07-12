package org.twins.core.featurer.fieldtyper;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamDouble;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamStringSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

import static org.cambium.common.util.MathUtils.EXPONENTIAL_FORM_REGEXP;

/**
 * Numeric decimal field contract: formatting/scaling parameters plus the shared
 * {@link #processAndFormatValue} / {@link #deserializeValueBase} algorithms. Extracted from
 * {@link FieldTyperDecimalBase} so both the standalone {@link FieldTyperDecimal} (built on
 * {@link FieldTyperSingleValue}) and the Mater hierarchy (via {@link FieldTyperDecimalBase})
 * share a single source of truth without copy-paste. Mirrors the {@link FieldTyperScalable} /
 * {@link FieldTyperCalcBinary} pattern of "featurer params + default helpers on an interface".
 */
public interface FieldTyperNumeric extends FieldTyperScalable {

    Logger log = LoggerFactory.getLogger(FieldTyperNumeric.class);

    @FeaturerParam(name = "Min", description = "Min possible value", order = 1, optional = true, defaultValue = "-2147483648")
    FeaturerParamDouble min = new FeaturerParamDouble("min");

    @FeaturerParam(name = "Max", description = "Max possible value", order = 2, optional = true, defaultValue = "2147483647")
    FeaturerParamDouble max = new FeaturerParamDouble("max");

    @FeaturerParam(name = "Step", description = "Step of value change", order = 3, optional = true, defaultValue = "1")
    FeaturerParamDouble step = new FeaturerParamDouble("step");

    @FeaturerParam(name = "Thousand separator", description = "Thousand separator. Must not be equal to decimal separator.", order = 4, optional = true, defaultValue = " ")
    FeaturerParamString thousandSeparator = new FeaturerParamString("thousandSeparator");

    @FeaturerParam(name = "Decimal separator", description = "Decimal separator. Must not be equal to thousand separator.", order = 5, optional = true, defaultValue = ".")
    FeaturerParamString decimalSeparator = new FeaturerParamString("decimalSeparator");

    @FeaturerParam(name = "Extra thousand separators", description = "Extra thousand separators. Must not be equal to decimal separator.", order = 7, optional = true)
    FeaturerParamStringSet extraThousandSeparatorSet = new FeaturerParamStringSet("extraThousandSeparatorSet");

    @FeaturerParam(name = "Extra decimal separators", description = "Extra decimal separators. Must not be equal to thousand separator.", order = 8, optional = true)
    FeaturerParamStringSet extraDecimalSeparatorSet = new FeaturerParamStringSet("extraDecimalSeparatorSet");

    @FeaturerParam(name = "Round", description = "Round a number to the required number of decimal places", order = 9, optional = true, defaultValue = "true")
    FeaturerParamBoolean round = new FeaturerParamBoolean("round");

    /**
     * Parse + validate + normalize a raw text value into the canonical decimal string form,
     * applying separator handling, decimal-places padding/truncation and min/max range checks.
     * Throws {@link ServiceException} if the value is not a numeric format or violates the settings.
     */
    default String processAndFormatValue(Properties properties, FieldValueText value) throws ServiceException {
        TwinClassFieldEntity twinClassField = value.getTwinClassField();
        var minValue = min.extract(properties);
        var maxValue = max.extract(properties);
        var stepValue = step.extract(properties);
        var thousandSeparatorValue = thousandSeparator.extract(properties);
        var decimalSeparatorValue = decimalSeparator.extract(properties);
        var extraThousandSeparators = Optional.ofNullable(extraThousandSeparatorSet.extract(properties)).orElse(Collections.emptySet());
        var extraDecimalSeparators = Optional.ofNullable(extraDecimalSeparatorSet.extract(properties)).orElse(Collections.emptySet());
        var decimalPlacesValue = decimalPlaces.extract(properties);
        var roundValue = round.extract(properties);
        var returnValue = value.getValue();

        try {
            if (Strings.isNotEmpty(returnValue)) {
                if (returnValue.matches(EXPONENTIAL_FORM_REGEXP)) {
                    DecimalFormat df = new DecimalFormat("#.############");
                    returnValue = df.format(Double.parseDouble(returnValue));
                }

                // Combine main decimal separator with extra ones for counting
                Set<String> allDecimalSeparators = new HashSet<>();
                allDecimalSeparators.add(decimalSeparatorValue);
                allDecimalSeparators.addAll(extraDecimalSeparators);

                int decimalSeparatorCount = 0;
                for (String decimalSep : allDecimalSeparators) {
                    decimalSeparatorCount += StringUtils.countMatches(returnValue, decimalSep);
                }
                if (decimalSeparatorCount > 1) {
                    log.error("FieldTyperNumeric: value[{}] has multiple decimal separators", value.getValue());
                    throw new Exception();
                }

                // Combine main thousand separator with extra ones for removal
                Set<String> allThousandSeparators = new HashSet<>();
                allThousandSeparators.add(thousandSeparatorValue);
                allThousandSeparators.addAll(extraThousandSeparators);

                // Remove all thousand separators
                for (String thousandSep : allThousandSeparators) {
                    returnValue = returnValue.replaceAll(Pattern.quote(thousandSep), "");
                }

                // Replace all decimal separators with dot
                for (String decimalSep : allDecimalSeparators) {
                    returnValue = returnValue.replaceAll(Pattern.quote(decimalSep), ".");
                }

                String[] parts = returnValue.split("\\.");
                String integerPart = parts[0];
                String decimalPart = parts.length > 1 ? parts[1] : "";

                if (decimalPart.length() > decimalPlacesValue) {
                    if (Boolean.FALSE.equals(roundValue)) {
                        log.error("FieldTyperNumeric: value[{}] has more decimal places then parametrized", value.getValue());
                        throw new Exception();
                    }
                    decimalPart = decimalPart.substring(0, decimalPlacesValue);
                } else if (decimalPart.length() < decimalPlacesValue) {
                    decimalPart = StringUtils.rightPad(decimalPart, decimalPlacesValue, '0');
                }

                if (decimalPlacesValue > 0) {
                    returnValue = integerPart + "." + decimalPart;
                } else {
                    returnValue = integerPart;
                }

                double doubleValue = Double.parseDouble(returnValue);
                if ((minValue != null && doubleValue < minValue) || (maxValue != null && doubleValue > maxValue)) {
                    log.error("FieldTyperNumeric: value[{}] is out of range", value.getValue());
                    throw new Exception();
                }
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    twinClassField.easyLog(EasyLoggable.Level.NORMAL) +
                            " value[" + value.getValue() + "] is not numeric format or does not match the field settings[" +
                            " min:" + minValue + " max:" + maxValue + " step:" + stepValue + " decPlaces:" + decimalPlacesValue +
                            " thousandSep:" + thousandSeparatorValue + " extraThousandSep:" + extraThousandSeparators +
                            " decimalSep:" + decimalSeparatorValue + " extraDecimalSep:" + extraDecimalSeparators + "].");
        }

        return returnValue;
    }

    /**
     * Read a stored {@link TwinFieldDecimalEntity} back into a {@link FieldValueText}, formatting
     * the {@link BigDecimal} with the configured decimal places (scale). A null entity or null
     * value yields a null string (treated as absent on read).
     */
    default FieldValueText deserializeValueBase(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldDecimalEntity) throws ServiceException {
        var scale = decimalPlaces.extract(properties);
        String value;

        if (scale != null) {
            value = twinFieldDecimalEntity != null && twinFieldDecimalEntity.getValue() != null
                    ? twinFieldDecimalEntity.getValue().setScale(scale, RoundingMode.UNNECESSARY).toPlainString()
                    : null;
        } else {
            value = twinFieldDecimalEntity != null && twinFieldDecimalEntity.getValue() != null
                    ? twinFieldDecimalEntity.getValue().toString()
                    : null;
        }

        return new FieldValueText(twinField.getTwinClassField()).setValue(value);
    }
}
