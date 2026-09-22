package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_23,
        name = "Filler",
        description = "")
@Slf4j
public abstract class Filler extends FeaturerTwins {
    @Lazy
    @Autowired
    FieldLookupers fieldLookupers;

    public void fill(HashMap<String, String> fillerParams, FactoryItemsBatch batch, TwinEntity templateTwin, String logMsg, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        Properties properties = featurerService.extractProperties(this, fillerParams);
        log.info("{}: running filler[{}] for {} factory item(s) with params: {}", logMsg, this.getClass().getSimpleName(), batch.size(), properties);
        fill(properties, batch, templateTwin, optionalStep);
    }

    public abstract void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException;

    public boolean canBeOptional() {
        return true; // most steps can be option by default. otherwise method must be overridden
    }


}
