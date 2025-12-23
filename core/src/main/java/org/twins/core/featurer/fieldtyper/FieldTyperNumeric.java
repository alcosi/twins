package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamDouble;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
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
    @FeaturerParam(name = "Thousand separator", description = "Thousand separator. Must not be equal to decimal separator.", order = 4)
    public static final FeaturerParamString thousandSeparator = new FeaturerParamString("thousandSeparator");
    @FeaturerParam(name = "Decimal separator", description = "Decimal separator. Must not be equal to thousand separator.", order = 5)
    public static final FeaturerParamString decimalSeparator = new FeaturerParamString("decimalSeparator");
    @FeaturerParam(name = "Decimal places", description = "Number of decimal places.", order = 6)
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
        Double minValue = min.extract(properties);
        Double maxValue = max.extract(properties);
        Double stepValue = step.extract(properties);
        String thousandSeparatorValue = thousandSeparator.extract(properties);
        String decimalSeparatorValue = decimalSeparator.extract(properties);
        Integer decimalPlacesValue = decimalPlaces.extract(properties);
        String finalValue;
        try {
            finalValue = value.getValue();
            if (!StringUtils.isBlank(finalValue)) {
                if (finalValue.matches(EXPONENTIAL_FORM_REGEXP)) {
                    DecimalFormat df = new DecimalFormat("#.############");
                    finalValue = df.format(Double.parseDouble(finalValue));
                }
                finalValue.replaceAll(Pattern.quote(thousandSeparatorValue), "")
                        .replaceAll(Pattern.quote(decimalSeparatorValue), ".");
                String[] parts = finalValue.split("\\.");
                if ((null != decimalPlacesValue && parts.length > 1 && parts[1].length() > decimalPlacesValue)) {
                    log.error("FieldTyperNumeric: value[" + value.getValue() + "] has more decimal places then parametrized");
                    throw new Exception();
                }
                double doubleValue = Double.parseDouble(finalValue);
                if ((null != minValue && doubleValue < minValue) || (null != maxValue && doubleValue > maxValue)) {
                    log.error("FieldTyperNumeric: value[" + value.getValue() + "] is less minimum value or greater then max value");
                    throw new Exception();
                }
            } // else value setting null
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

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchNumeric search) throws ServiceException {
        return TwinSpecification.checkFieldNumeric(search);
    }
}
