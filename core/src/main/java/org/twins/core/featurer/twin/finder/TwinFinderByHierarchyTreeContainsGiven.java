package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinId;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2711,
        name = "By hierarchy tree contains twin id (given)",
        description = "")
public class TwinFinderByHierarchyTreeContainsGiven extends TwinFinder {
    @FeaturerParam(name = "Head twin ids", description = "", order = 1)
    public static final FeaturerParamUUID headTwinId = new FeaturerParamUUIDTwinsTwinId("twinIds");

    @Override
    public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        twinSearch.addHierarchyTreeContainsId(headTwinId.extract(properties));
    }
}
