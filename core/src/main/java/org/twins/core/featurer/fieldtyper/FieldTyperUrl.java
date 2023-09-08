package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@Featurer(id = 1303,
        name = "FieldTyperUrl",
        description = "")
public class FieldTyperUrl extends FieldTyper<FieldValueText> {
       @Override
    public FieldTypeUIDescriptor getUiDescriptor(Properties properties) {
        return new FieldTypeUIDescriptor()
                .type("url");
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
