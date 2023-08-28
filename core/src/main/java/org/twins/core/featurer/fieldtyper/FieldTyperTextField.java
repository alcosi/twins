package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.dao.FeaturerInjectionEntity;
import org.cambium.featurer.injectors.Injector;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

@Component
@Featurer(id = 1301,
        name = "FieldTyperTextField",
        description = "")
public class FieldTyperTextField extends FieldTyper {
    @FeaturerParam(name = "regexp", description = "")
    public static final FeaturerParamString regexp = new FeaturerParamString("regexp");

    @Override
    public String getType() {
        return "textField";
    }

    @Override
    public Hashtable<String, String> getUiParamList(Properties properties) {
        Hashtable<String, String> params = new Hashtable<>();
        params.put("regexp", regexp.extract(properties));
        return params;
    }
}
