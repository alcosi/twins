package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2716,
        name = "Link",
        description = "")
public class TwinFinderByLinkDstTwinGiven extends TwinFinder {
    @FeaturerParam(name = "Link id", description = "", order = 1)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "Dst twin id", description = "", order = 2)
    public static final FeaturerParamUUIDSet dstTwinId = new FeaturerParamUUIDSetTwinsTwinId("dstTwinId");

    @FeaturerParam(name = "Exclude", description = "", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @FeaturerParam(name = "Any of list", description = "", order = 3)
    public static final FeaturerParamBoolean anyOfList = new FeaturerParamBoolean("anyOfList");

    @Override
    public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        twinSearch.addLinkDstTwinsId(linkId.extract(properties), dstTwinId.extract(properties), exclude.extract(properties), anyOfList.extract(properties));
    }
}
