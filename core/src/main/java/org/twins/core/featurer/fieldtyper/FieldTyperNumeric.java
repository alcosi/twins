package org.twins.core.featurer.fieldtyper;

import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = 1317,
        name = "FieldTyperNumeric",
        description = "")
public class FieldTyperNumeric extends FieldTyper<FieldDescriptorNumeric, FieldValueText> {

    @FeaturerParam(name = "min", description = "Min possible value")
    public static final FeaturerParamInt min = new FeaturerParamInt("min");
    @FeaturerParam(name = "max", description = "Max possible value")
    public static final FeaturerParamInt max = new FeaturerParamInt("max");
    @FeaturerParam(name = "step", description = "Step of value change")
    public static final FeaturerParamInt step = new FeaturerParamInt("step");
    @FeaturerParam(name = "thousandSeparator", description = "Thousand separator")
    public static final FeaturerParamString thousandSeparator = new FeaturerParamString("thousandSeparator");

    @Override
    public FieldDescriptorNumeric getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorNumeric()
                .min(min.extract(properties))
                .max(max.extract(properties))
                .step(step.extract(properties))
                .thousandSeparator(thousandSeparator.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinFieldEntity.getTwinClassField().isRequired() && StringUtils.isEmpty(value.getValue()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        String pattern = regexp.extract(properties);
        if (!value.getValue().matches(pattern))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " value[" + value.getValue() + "] does not match pattern[" + pattern + "]");
        detectValueChange(twinFieldEntity, twinChangesCollector, value.getValue());
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinFieldEntity twinFieldEntity) {
        return new FieldValueText().setValue(twinFieldEntity.getValue() != null ? twinFieldEntity.getValue() : "");
    }
}
