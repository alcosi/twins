package org.twins.core.featurer.classfield.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3201,
        name = "All class fields",
        description = "")
public class FieldFinderAll extends FieldFinder {
    @FeaturerParam(name = "Exclude system fields", description = "", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean excludeSystemFields = new FeaturerParamBoolean("excludeSystemFields");

    @Override
    protected void concatSearch(Properties properties, TwinClassFieldSearch fieldSearch, Map<String, String> namedParamsMap) throws ServiceException {
        fieldSearch.setExcludeSystemFields(excludeSystemFields.extract(properties));
    }
}
