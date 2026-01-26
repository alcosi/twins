package org.twins.core.featurer.classfinder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3902,
        name = "Class finder by given twin class id set",
        description = "")
public class ClassFinderGivenSet extends ClassFinder {
    @FeaturerParam(name = "twin class id set", description = "", order = 1, optional = false)
    public static final FeaturerParamUUIDSet twinClassIds = new FeaturerParamUUIDSetTwinsClassId("twinClassIdSet");

    @FeaturerParam(name = "Exclude given classes", description = "", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean excludeGivenIds = new FeaturerParamBoolean("excludeGivenIds");

    @Override
    protected void concatSearch(Properties properties, TwinClassSearch classSearch) throws ServiceException {
        Set<UUID> twinClassIdSet = twinClassIds.extract(properties);
        if (twinClassIdSet.isEmpty()) {
            return;
        }
        classSearch.addTwinClassId(twinClassIdSet, excludeGivenIds.extract(properties));
    }
}
