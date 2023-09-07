package org.twins.core.featurer.fieldtyper;

import org.apache.commons.validator.GenericValidator;
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

    @Override
    protected FieldValue deserializeValue(Properties properties, Object value) {
        return new FieldValueDate().date(value != null ? validDateOrEmpty(value.toString(), properties) : "");
    }

    public String validDateOrEmpty(String dateStr, Properties properties) {
        if (GenericValidator.isDate(dateStr, pattern.extract(properties), true))
            return dateStr;
        return "";
    }
}
