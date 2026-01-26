package org.twins.core.featurer.user.finder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
public abstract class UserFinderRequested extends UserFinder {
    public static final String PARAM_SPACE_ID = "spaceId";

    @FeaturerParam(name = "Required", description = "", order = 10, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean required = new FeaturerParamBoolean("required");

    public UUID getRequestedId(FeaturerParamString paramKey, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        if (namedParamsMap == null) {
            namedParamsMap = Collections.EMPTY_MAP;
        }
        String paramKeyStr = paramKey.extract(properties);
        String paramValue = namedParamsMap.get(paramKeyStr);
        if (StringUtils.isBlank(paramValue)) {
            if (required.extract(properties))
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED, "search param[" + paramKeyStr + "] missed");
            else
                return null;
        }
        try {
            return UUID.fromString(paramValue);
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "search param[" + paramKeyStr + "] is not uuid (or uuid list)");
        }
    }
}
