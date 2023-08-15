package org.cambium.featurer.injectors;

import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.dao.FeaturerInjectionEntity;

import java.util.HashMap;
import java.util.Properties;

@FeaturerType(id = 10,
        name = "injectors",
        description = "Выполняет подстановку параметров для Featurer")
public abstract class Injector extends Featurer {
    public String doInject(FeaturerInjectionEntity injection, HashMap<String, Object> context) throws Exception {
        Properties injectorProperties = featurerService.extractProperties(injection.getInjectorFeaturer(), injection.getInjectorParams(), context);
        return inject(injection, injectorProperties, context);
    }

    protected abstract String inject(FeaturerInjectionEntity injection, Properties properties, HashMap<String, Object> context) throws Exception;
}
