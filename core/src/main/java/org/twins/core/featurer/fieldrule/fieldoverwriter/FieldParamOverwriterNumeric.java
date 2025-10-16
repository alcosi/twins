package org.twins.core.featurer.fieldrule.fieldoverwriter;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamDouble;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4604,
        name = "Field Overwriter Numeric",
        description = "Overwrite a numeric field with a specified params")
public class FieldParamOverwriterNumeric extends FieldParamOverwriter<FieldDescriptorNumeric> {
    @FeaturerParam(name = "Min", description = "Min possible value", order = 1, optional = true)
    public static final FeaturerParamDouble min = new FeaturerParamDouble("min");
    @FeaturerParam(name = "Max", description = "Max possible value", order = 2, optional = true)
    public static final FeaturerParamDouble max = new FeaturerParamDouble("max");
    @FeaturerParam(name = "Step", description = "Step of value change", order = 3, optional = true)
    public static final FeaturerParamDouble step = new FeaturerParamDouble("step");
    @FeaturerParam(name = "Thousand separator", description = "Thousand separator. Must not be equal to decimal separator.", order = 4, optional = true)
    public static final FeaturerParamString thousandSeparator = new FeaturerParamString("thousandSeparator");
    @FeaturerParam(name = "Decimal separator", description = "Decimal separator. Must not be equal to thousand separator.", order = 5, optional = true)
    public static final FeaturerParamString decimalSeparator = new FeaturerParamString("decimalSeparator");
    @FeaturerParam(name = "Decimal places", description = "Number of decimal places.", order = 6, optional = true)
    public static final FeaturerParamInt decimalPlaces = new FeaturerParamInt("decimalPlaces");

    @Override
    protected FieldDescriptorNumeric getFieldOverwriterDescriptor(TwinClassFieldRuleEntity twinClassFieldRuleEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorNumeric()
                .min(min.extract(properties))
                .max(max.extract(properties))
                .step(step.extract(properties))
                .thousandSeparator(thousandSeparator.extract(properties))
                .decimalSeparator(decimalSeparator.extract(properties))
                .decimalPlaces(decimalPlaces.extract(properties));
    }
}
