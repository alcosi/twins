package org.twins.core.featurer.classfield.finder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3207,
        name = "",
        description = "")
public class FieldFinderByClassFromParam extends FieldFinder {
    @FeaturerParam(name = "extends hierarchy check", description = "", order = 1, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean searchExtends = new FeaturerParamBoolean("searchExtends");

    @Override
    protected void concatSearch(Properties properties, TwinClassFieldSearch fieldSearch, Map<String, String> namedParamsMap) throws ServiceException {
        String paramValue = namedParamsMap.get(PARAM_CURRENT_TWIN_CLASS_ID);
        if (StringUtils.isBlank(paramValue))
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED, "search param[" + PARAM_CURRENT_TWIN_CLASS_ID + "] missed");
        if (!UuidUtils.isUUID(paramValue)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED, "search param[" + PARAM_CURRENT_TWIN_CLASS_ID + "] is not UUID");
        }
        fieldSearch.addTwinClassId(UUID.fromString(namedParamsMap.get(PARAM_CURRENT_TWIN_CLASS_ID)), searchExtends.extract(properties), false);
    }
}