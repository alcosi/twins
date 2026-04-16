package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldValueSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

import static org.cambium.common.util.MathUtils.EXPONENTIAL_FORM_REGEXP;

@Slf4j
public abstract class FieldTyperDecimalBase<D extends FieldDescriptor, T extends FieldValue, A extends TwinFieldValueSearch> extends FieldTyper<D, T, TwinFieldStorageDecimal, A> {

    @FeaturerParam(name = "Min", description = "Min possible value", order = 1, optional = true, defaultValue = "-2147483648")
    public static final FeaturerParamDouble min = new FeaturerParamDouble("min");

    @FeaturerParam(name = "Max", description = "Max possible value", order = 2, optional = true, defaultValue = "2147483647")
    public static final FeaturerParamDouble max = new FeaturerParamDouble("max");

    @FeaturerParam(name = "Step", description = "Step of value change", order = 3, optional = true, defaultValue = "1")
    public static final FeaturerParamDouble step = new FeaturerParamDouble("step");

    @FeaturerParam(name = "Thousand separator", description = "Thousand separator. Must not be equal to decimal separator.", order = 4, optional = true, defaultValue = " ")
    public static final FeaturerParamString thousandSeparator = new FeaturerParamString("thousandSeparator");

    @FeaturerParam(name = "Decimal separator", description = "Decimal separator. Must not be equal to thousand separator.", order = 5, optional = true, defaultValue = ".")
    public static final FeaturerParamString decimalSeparator = new FeaturerParamString("decimalSeparator");

    @FeaturerParam(name = "Decimal places", description = "Number of decimal places.", order = 6, optional = true, defaultValue = "0")
    public static final FeaturerParamInt decimalPlaces = new FeaturerParamInt("decimalPlaces");

    @FeaturerParam(name = "Extra thousand separators", description = "Extra thousand separators. Must not be equal to decimal separator.", order = 7, optional = true)
    public static final FeaturerParamStringSet extraThousandSeparatorSet = new FeaturerParamStringSet("extraThousandSeparatorSet");

    @FeaturerParam(name = "Extra decimal separators", description = "Extra decimal separators. Must not be equal to thousand separator.", order = 8, optional = true)
    public static final FeaturerParamStringSet extraDecimalSeparatorSet = new FeaturerParamStringSet("extraDecimalSeparatorSet");

    @FeaturerParam(name = "Round", description = "Round a number to the required number of decimal places", order = 9, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean round = new FeaturerParamBoolean("round");

    protected abstract void serializeValue(Properties properties, TwinEntity twin, TwinFieldDecimalEntity twinFieldEntity, T value, TwinChangesCollector twinChangesCollector) throws ServiceException;
    protected abstract T deserializeValue(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldDecimalEntity) throws ServiceException;

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        var twinFieldDecimalEntity = convertToTwinFieldEntity(twin, value.getTwinClassField());
        serializeValue(properties, twin, twinFieldDecimalEntity, value, twinChangesCollector);
    }

    @Override
    protected T deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        var twinFieldDecimalEntity = convertToTwinFieldEntity(twinField.getTwin(), twinField.getTwinClassField());
        return deserializeValue(properties, twinField, twinFieldDecimalEntity);
    }

    private TwinFieldDecimalEntity convertToTwinFieldEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        twinService.loadTwinFields(twinEntity);
        return twinEntity.getTwinFieldDecimalKit().get(twinClassFieldEntity.getId());
    }

    protected void detectValueChange(TwinFieldDecimalEntity twinFieldDecimalEntity, TwinChangesCollector twinChangesCollector, BigDecimal newValue) {
        if (twinChangesCollector.collectIfChangedWithNullifySupport(twinFieldDecimalEntity, "field[" + twinFieldDecimalEntity.getTwinClassField().getKey() + "]", twinFieldDecimalEntity.getValue(), newValue)) {
            addHistoryContext(twinChangesCollector,  twinFieldDecimalEntity, newValue);
            twinFieldDecimalEntity.setValue(newValue);
        }
    }

    protected void addHistoryContext(TwinChangesCollector twinChangesCollector, TwinFieldDecimalEntity twinFieldDecimalEntity, BigDecimal newValue) {
        if (twinChangesCollector.isHistoryCollectorEnabled()) {
            twinChangesCollector
                    .getHistoryCollector(twinFieldDecimalEntity.getTwin())
                    .add(
                            historyService.fieldChangeDecimal(
                                    twinFieldDecimalEntity.getTwinClassField(),
                                    twinFieldDecimalEntity.getValue() != null ? twinFieldDecimalEntity.getValue() : null,
                                    newValue
                            )
                    );
        }
    }

    protected T deserializeValueBase(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldDecimalEntity) throws ServiceException {
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

        T ret = (T) new FieldValueText(twinField.getTwinClassField()).setValue(value);
        return ret;
    }

    protected String processAndFormatValue(Properties properties, FieldValueText value) throws ServiceException {
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
}

