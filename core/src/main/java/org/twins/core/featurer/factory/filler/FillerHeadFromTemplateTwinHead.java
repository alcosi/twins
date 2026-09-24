package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinHeadService;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2306,
        name = "Head from template twin head",
        description = "")
@Slf4j
public class FillerHeadFromTemplateTwinHead extends Filler {
    @Lazy
    @Autowired
    TwinService twinService;

    /**
     * Direct batch override (not a {@code FillerAtomic} subclass): the template twin is shared by the
     * whole batch, so the template checks and the head load run once per step instead of once per item.
     */
    @Override
    public void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        if (templateTwin == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Empty template twin");
        if (templateTwin.getHeadTwinId() == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Empty template head twin");
        twinService.loadHead(templateTwin); // once per batch
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                var outputTwin = factoryItem.getOutput().getTwinEntity();
                TwinHeadService.setHead(outputTwin, templateTwin.getHeadTwin());
            } catch (Exception ex) {
                if (optionalStep) {
                    log.warn("Step is optional and unsuccessful for {}: {}. Pipeline will not be aborted",
                            factoryItem.logShort(),
                            ex instanceof ServiceException serviceException ? serviceException.getErrorLocation() : ex.getMessage());
                } else {
                    throw ex;
                }
            }
        }
    }
}
