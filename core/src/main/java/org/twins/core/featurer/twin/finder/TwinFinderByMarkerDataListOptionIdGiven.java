package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2720,
        name = "By marker datalist option ids",
        description = "")
public class TwinFinderByMarkerDataListOptionIdGiven extends TwinFinder {
    @FeaturerParam(name = "Marker datalist option ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet markerDataListOptionIds = new FeaturerParamUUIDSet("markerDataListOptionIds");

    @FeaturerParam(name = "Exclude", description = "", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @Override
    public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        twinSearch.addMarkerDataListOptionId(markerDataListOptionIds.extract(properties), exclude.extract(properties));
    }
}
