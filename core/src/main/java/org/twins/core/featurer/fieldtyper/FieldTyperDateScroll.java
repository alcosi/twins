package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@Featurer(id = 1302,
        name = "FieldTyperDateScroll",
        description = "")
public class FieldTyperDateScroll extends FieldTyper {
    @FeaturerParam(name = "pattern", description = "")
    public static final FeaturerParamString pattern = new FeaturerParamString("pattern");

    @Override
    public FieldTypeUIDescriptor getUiDescriptor(Properties properties) {
        return new FieldTypeUIDescriptor()
                .type("dateScroll")
                .addParam("pattern", pattern.extract(properties));
    }
}
