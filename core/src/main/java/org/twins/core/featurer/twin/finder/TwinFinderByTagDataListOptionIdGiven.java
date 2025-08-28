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
@Featurer(id = FeaturerTwins.ID_2723,
        name = "By tag datalist option ids",
        description = "")
public class TwinFinderByTagDataListOptionIdGiven extends TwinFinder {
    @FeaturerParam(name = "Tag datalist option ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet tagDataListOptionIds = new FeaturerParamUUIDSet("tagDataListOptionIds");

    @FeaturerParam(name = "Exclude", description = "", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @Override
    public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        twinSearch.addTagDataListOptionId(tagDataListOptionIds.extract(properties), exclude.extract(properties));
    }
}
