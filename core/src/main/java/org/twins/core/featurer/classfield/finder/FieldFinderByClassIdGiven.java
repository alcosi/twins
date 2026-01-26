package org.twins.core.featurer.classfield.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3206,
        name = "Given fields by class id",
        description = "")
public class FieldFinderByClassIdGiven extends FieldFinder {
    @FeaturerParam(name = "Class ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet classIds = new FeaturerParamUUIDSetTwinsClassId("classIds");

    @FeaturerParam(name = "Exclude given classes", description = "", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean excludeClassesIds = new FeaturerParamBoolean("excludeClassesIds");

    @FeaturerParam(name = "Search extends", description = "", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean searchExtends = new FeaturerParamBoolean("searchExtends");

    @Override
    protected void concatSearch(Properties properties, TwinClassFieldSearch fieldSearch, Map<String, String> namedParamsMap) throws ServiceException {
        fieldSearch.addTwinClassId(classIds.extract(properties), searchExtends.extract(properties), excludeClassesIds.extract(properties));
    }
}
