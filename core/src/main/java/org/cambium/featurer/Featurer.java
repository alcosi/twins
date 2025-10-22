package org.cambium.featurer;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
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
        Properties properties = featurerService.extractProperties(this, paramsMap, Collections.emptyMap());
        if (logParams)
            log.info("Running featurer[{}] with params: {}", this.getClass().getSimpleName(), properties.toString());
        return properties;
    }

    public Properties extractProperties(HashMap<String, String> paramsMap) throws ServiceException {
        return featurerService.extractProperties(this, paramsMap, Collections.emptyMap());
    }

    public void extraParamsValidation(Properties properties) throws ServiceException {
    }
}
