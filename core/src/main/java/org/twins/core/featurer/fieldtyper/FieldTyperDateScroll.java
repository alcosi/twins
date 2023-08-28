package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.Properties;

@Component
@Featurer(id = 1302,
        name = "FieldTyperDateScroll",
        description = "")
public class FieldTyperDateScroll extends FieldTyper {
    @FeaturerParam(name = "pattern", description = "")
    public static final FeaturerParamString pattern = new FeaturerParamString("pattern");

    @Override
    public String getType() {
        return "dateScroll";
    }

    @Override
    public Hashtable<String, String> getUiParamList(Properties properties) {
        Hashtable<String, String> params = new Hashtable<>();
        params.put("pattern", pattern.extract(properties));
        return params;
    }
}
