package org.twins.core.featurer.classfield.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.Ternary;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3203,
        name = "Required fields",
        description = "")
public class FieldFinderByRequiredTrue extends FieldFinder {

    @Override
    protected void concatSearch(Properties properties, TwinClassFieldSearch fieldSearch, Map<String, String> namedParamsMap) throws ServiceException {
        fieldSearch.setRequired(Ternary.ONLY);

    }
}
