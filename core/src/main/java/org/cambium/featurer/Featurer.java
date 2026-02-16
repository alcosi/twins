package org.cambium.featurer;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


@Slf4j
public abstract class Featurer {
    @Autowired
    public FeaturerService featurerService;

    @PostConstruct
    private void postConstruct() {
        //check and update feature components in database
    }

    public Properties extractProperties(HashMap<String, String> paramsMap, boolean logParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, paramsMap);
        if (logParams)
            log.info("Running featurer[{}] with params: {}", this.getClass().getSimpleName(), properties.toString());
        return properties;
    }

    public Properties extractProperties(HashMap<String, String> paramsMap) throws ServiceException {
        return featurerService.extractProperties(this, paramsMap);
    }

    public void extraParamsValidation(Properties properties) throws ServiceException {
    }

    protected static List<Type> collectParameterizedTypes(Class<?> _class, List<Type> collected) {
        Type t = _class.getGenericSuperclass();
        if (t instanceof ParameterizedType pt) {
            collected.addAll(Arrays.asList(pt.getActualTypeArguments()));
        }
        if (_class.getSuperclass() == null)
            return collected;
        return collectParameterizedTypes(_class.getSuperclass(), collected);
    }
}
