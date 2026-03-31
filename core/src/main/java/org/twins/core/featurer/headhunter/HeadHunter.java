package org.twins.core.featurer.headhunter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_26,
        name = "HeadHunter",
        description = "Getting valid head twin class by some class")
@Slf4j
public abstract class HeadHunter extends FeaturerTwins {
    public void expandValidHeadSearch(HashMap<String, String> headHunterParams, TwinClassEntity twinClassEntity, BasicSearch basicSearch) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, headHunterParams);
        log.info("Running featurer[" + this.getClass().getSimpleName() + "].expandValidHeadSearch with params: " + properties.toString());
        expandValidHeadSearch(properties, twinClassEntity, basicSearch);
    }

    protected abstract void expandValidHeadSearch(Properties properties, TwinClassEntity twinClassEntity, BasicSearch basicSearch) throws ServiceException;

    /**
     * Method to check if some new twin of given class can be created as child for given twin.
     * Important, that permissions are checked before this method call
     * @param headHunterParams
     * @param twinEntity
     * @param twinClassEntity
     * @return
     * @throws ServiceException
     */
    public boolean isCreatableChildClass(HashMap<String, String> headHunterParams, TwinEntity twinEntity, TwinClassEntity twinClassEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, headHunterParams);
        log.info("Running featurer[" + this.getClass().getSimpleName() + "].isCreatableChildClass with params: " + properties.toString());
        return isCreatableChildClass(properties, twinEntity, twinClassEntity);
    }

    protected abstract boolean isCreatableChildClass(Properties properties, TwinEntity twinEntity, TwinClassEntity twinClassEntity) throws ServiceException;
}
