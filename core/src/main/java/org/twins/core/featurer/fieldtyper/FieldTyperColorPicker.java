package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@Featurer(id = 1304,
        name = "FieldTyperColorPicker",
        description = "")
public class FieldTyperColorPicker extends FieldTyper<FieldValueColorHEX> {
    @Override
    public FieldTypeUIDescriptor getUiDescriptor(Properties properties) {
        return new FieldTypeUIDescriptor()
                .type("colorPicker");
    }

    @Override
    protected String serializeValue(Properties properties, FieldValueColorHEX value) {
        return value.hex();
    }

    @Override
    protected FieldValueColorHEX deserializeValue(Properties properties, Object value) {
        return new FieldValueColorHEX().hex(value != null ? value.toString() : "");
    }
}
