package org.twins.core.featurer.twinclass;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.data.domain.Pageable;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.service.twin.TwinSearchResult;

import java.util.HashMap;
import java.util.Properties;

@FeaturerType(id = 26,
        name = "HeadHunter",
        description = "Getting valid head twin class by some class")
@Slf4j
public abstract class HeadHunter extends Featurer {
    public TwinSearchResult findValidHead(HashMap<String, String> headHunterParams, TwinClassEntity twinClassEntity, Pageable pageable) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, headHunterParams, new HashMap<>());
        log.info("Running featurer[" + this.getClass().getSimpleName() + "] with params: " + properties.toString());
        return findValidHead(properties, twinClassEntity, pageable);
    }

    protected abstract TwinSearchResult findValidHead(Properties properties, TwinClassEntity twinClassEntity, Pageable pageable) throws ServiceException;
}
