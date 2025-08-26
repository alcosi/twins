package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2705,
        name = "By class id (requested)",
        description = "")
public abstract class TwinFinderByClassIdRequested extends TwinFinderRequested {
    @FeaturerParam(name = "Exclude", description = "", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @Override
    public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        Set<UUID> classIdSet = getRequestedIds(properties, namedParamsMap);
        if (CollectionUtils.isEmpty(classIdSet)) {
            return;
        }
        twinSearch.addTwinClassId(classIdSet, exclude.extract(properties));
    }
}
