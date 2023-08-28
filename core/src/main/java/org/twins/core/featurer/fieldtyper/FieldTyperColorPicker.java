package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.Properties;

@Component
@Featurer(id = 1304,
        name = "FieldTyperColorPicker",
        description = "")
public class FieldTyperColorPicker extends FieldTyper {
    @Override
    public String getType() {
        return "colorPicker";
    }

    @Override
    public Hashtable<String, String> getUiParamList(Properties properties) {
        return null;
    }
}
