package org.twins.core.featurer.factory.multiplier;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;


@FeaturerType(id = 22,
        name = "Multiplier",
        description = "")
@Slf4j
public abstract class Multiplier extends Featurer {
    @Lazy
    @Autowired
    TwinClassService twinClassService;

    @Lazy
    @Autowired
    AuthService authService;

    public List<FactoryItem> multiply(HashMap<String, String> multiplierParams, List<TwinEntity> input, FactoryContext factoryContext) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, multiplierParams, new HashMap<>());
        log.info("Running multiplier[" + this.getClass().getSimpleName() + "] with params: " + properties.toString());
        return multiply(properties, input, factoryContext);
    }

    public abstract List<FactoryItem> multiply(Properties properties, List<TwinEntity> inputTwinList, FactoryContext factoryContext) throws ServiceException;
}
