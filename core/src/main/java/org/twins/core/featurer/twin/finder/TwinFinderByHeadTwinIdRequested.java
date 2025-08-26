package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
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
@Featurer(id = FeaturerTwins.ID_2710,
        name = "By head twin id (requested)",
        description = "")
public class TwinFinderByHeadTwinIdRequested extends TwinFinderRequested {
    @Override
    public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        UUID headTwinId = getRequestedId(properties, namedParamsMap);
        if (headTwinId != null)
            twinSearch.addHeadTwinId(headTwinId);
    }
}
