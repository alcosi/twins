package org.twins.core.featurer.classfield.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3202,
        name = "Given fields by id",
        description = "")
public class FieldFinderByIdGiven extends FieldFinder {
    @FeaturerParam(name = "Field ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet fieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("fieldIds");

    @FeaturerParam(name = "Exclude given fields", description = "", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean excludeGivenIds = new FeaturerParamBoolean("excludeGivenIds");

    @Override
    protected void concatSearch(Properties properties, TwinClassFieldSearch fieldSearch, Map<String, String> namedParamsMap) throws ServiceException {
        if (excludeGivenIds.extract(properties)) {
            fieldSearch.setIdExcludeList(CollectionUtils.safeAdd(fieldSearch.getIdExcludeList(), fieldIds.extract(properties)));
        } else {
            fieldSearch.setIdList(CollectionUtils.safeAdd(fieldSearch.getIdList(), fieldIds.extract(properties)));
        }
    }
}
