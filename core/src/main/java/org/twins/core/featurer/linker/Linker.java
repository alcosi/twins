package org.twins.core.featurer.linker;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_30,
        name = "Linker",
        description = "Getting valid twins for link")
@Slf4j
public abstract class Linker extends FeaturerTwins {
    public void expandValidLinkedTwinSearch(HashMap<String, String> linkerParams, TwinClassEntity twinClassEntity, TwinEntity headTwinEntity, BasicSearch basicSearch) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, linkerParams);
        log.info("Running featurer[" + this.getClass().getSimpleName() + "].expandValidLinkedTwinSearch with params: " + properties.toString());
        expandValidLinkedTwinSearch(properties, twinClassEntity, headTwinEntity, basicSearch);
    }

    protected abstract void expandValidLinkedTwinSearch(Properties properties, TwinClassEntity twinClassEntity, TwinEntity headTwinEntity, BasicSearch basicSearch) throws ServiceException;

    public void expandValidLinkedTwinSearch(HashMap<String, String> linkerParams, TwinEntity twinEntity, BasicSearch basicSearch) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, linkerParams);
        log.info("Running featurer[" + this.getClass().getSimpleName() + "].expandValidLinkedTwinSearch with params: " + properties.toString());
        expandValidLinkedTwinSearch(properties, twinEntity, basicSearch);
    }

    public abstract void expandValidLinkedTwinSearch(Properties properties, TwinEntity twinEntity, BasicSearch basicSearch);
}
