package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
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

    /**
     * Optional-step error isolation of one factory item, shared by every batch loop: a failing item
     * of an optional step is logged and skipped, a failing item of a mandatory step aborts the batch
     * (fail-fast — end state identical to the old per-item caller after rollback).
     */
    protected void handleItemError(FactoryItem factoryItem, boolean optionalStep, Exception ex) throws ServiceException {
        if (optionalStep) {
            log.warn("Step is optional and unsuccessful for {}: {}. Pipeline will not be aborted",
                    factoryItem.logShort(),
                    ex instanceof ServiceException serviceException ? serviceException.getErrorLocation() : ex.getMessage());
        } else {
            if (ex instanceof ServiceException serviceException)
                throw serviceException;
            if (ex instanceof RuntimeException runtimeException)
                throw runtimeException;
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, ex.getMessage());
        }
    }

    public boolean canBeOptional() {
        return true; // most steps can be option by default. otherwise method must be overridden
    }


}
