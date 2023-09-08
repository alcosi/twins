package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@Featurer(id = 1301,
        name = "FieldTyperTextField",
        description = "")
public class FieldTyperTextField extends FieldTyper<FieldValueText> {
    @FeaturerParam(name = "regexp", description = "")
    public static final FeaturerParamString regexp = new FeaturerParamString("regexp");

    @Override
    public FieldTypeUIDescriptor getUiDescriptor(Properties properties) {
        return new FieldTypeUIDescriptor()
                .type("textField")
                .addParam("regexp", regexp.extract(properties));
    }

    @Override
    protected String serializeValue(Properties properties, FieldValueText value) {
        return value.value();
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, Object value) {
        return new FieldValueText().value(value != null ? value.toString() : "");
    }
}
