package org.cambium.featurer.injectors;

import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.dao.FeaturerInjectionEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

@Component
@Featurer(id = 1001,
        name = "InjectorImpl",
        description = "")
public class InjectorImpl extends Injector {
    public String inject(FeaturerInjectionEntity injection, Properties properties, Map<String, Object> context) throws Exception {
        return "";
    }
}
