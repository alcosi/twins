package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.dao.FeaturerInjectionEntity;
import org.cambium.featurer.injectors.Injector;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Properties;

@Component
@Featurer(id = 1301,
        name = "FieldTyperTextField",
        description = "")
public class FieldTyperTextField extends FieldTyper {
    public String inject(FeaturerInjectionEntity injection, Properties properties, HashMap<String, Object> context) throws Exception {
        return "";
    }
}
