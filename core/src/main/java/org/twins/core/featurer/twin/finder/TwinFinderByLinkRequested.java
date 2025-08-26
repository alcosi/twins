package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2719,
        name = "By link id (given)",
        description = "")
public class TwinFinderByLinkRequested extends TwinFinderRequested {

    @FeaturerParam(name = "Exclude", description = "", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @FeaturerParam(name = "Any of list", description = "", order = 3)
    public static final FeaturerParamBoolean anyOfList = new FeaturerParamBoolean("anyOfList");

    @Override
    public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        UUID linkId = getRequestedId(properties, namedParamsMap);
        if (linkId != null) {
            twinSearch.addLinkDstTwinsId(linkId, null, exclude.extract(properties), anyOfList.extract(properties));
        }
    }
}
