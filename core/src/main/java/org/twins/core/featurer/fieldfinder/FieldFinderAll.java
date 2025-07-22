package org.twins.core.featurer.fieldfinder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3201,
        name = "All class fields",
        description = "")
public class FieldFinderAll extends FieldFinder {
    @Override
    protected void createSearch(Properties properties, TwinClassFieldSearch fieldSearch) throws ServiceException {
    }
}
